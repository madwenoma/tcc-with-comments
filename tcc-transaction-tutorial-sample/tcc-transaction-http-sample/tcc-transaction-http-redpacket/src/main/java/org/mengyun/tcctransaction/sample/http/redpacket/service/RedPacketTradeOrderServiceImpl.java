package org.mengyun.tcctransaction.sample.http.redpacket.service;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.context.MethodTransactionContextEditor;
import org.mengyun.tcctransaction.sample.http.redpacket.api.RedPacketTradeOrderService;
import org.mengyun.tcctransaction.sample.http.redpacket.api.dto.RedPacketTradeOrderDto;
import org.mengyun.tcctransaction.sample.redpacket.domain.entity.RedPacketAccount;
import org.mengyun.tcctransaction.sample.redpacket.domain.entity.TradeOrder;
import org.mengyun.tcctransaction.sample.redpacket.domain.repository.RedPacketAccountRepository;
import org.mengyun.tcctransaction.sample.redpacket.domain.repository.TradeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;

/**
 * Created by changming.xie on 4/2/16.
 */
public class RedPacketTradeOrderServiceImpl implements RedPacketTradeOrderService {

    Logger logger = LoggerFactory.getLogger(RedPacketTradeOrderServiceImpl.class);

    @Autowired
    RedPacketAccountRepository redPacketAccountRepository;

    @Autowired
    TradeOrderRepository tradeOrderRepository;

    /**
     * try阶段，
     * 1.插入交易记录
     * 2.预扣除账户红包（这里的预的含义是数额减少，但交易记录里的状态是DRAFT，等到confirm阶段更新）
     *
     * @param transactionContext
     * @param tradeOrderDto
     * @return
     */
    @Override
    @Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", transactionContextEditor = MethodTransactionContextEditor.class)
    @Transactional
    public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("red packet try record called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        TradeOrder foundTradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());
        logger.info("根据订单号查询交易记录{}", foundTradeOrder);

        //check if the trade order has need recorded.
        //if record, then this method call return success directly.
        if (foundTradeOrder == null) {

            TradeOrder tradeOrder = new TradeOrder(
                    tradeOrderDto.getSelfUserId(),
                    tradeOrderDto.getOppositeUserId(),
                    tradeOrderDto.getMerchantOrderNo(),
                    tradeOrderDto.getAmount()
            );

            try {
                //trying
                logger.info("交易记录为空，1.新生成交易记录{}，开始insert交易记录", tradeOrder);
                tradeOrderRepository.insert(tradeOrder);
                //付款user id查询账户
                RedPacketAccount transferFromAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());

                transferFromAccount.transferFrom(tradeOrderDto.getAmount());//付款用户红包账户扣减相应金额
                logger.info("根据dto传来的userid，查询到用户账户{}，2.开始扣减红包，更新红包账户信息", transferFromAccount);

                redPacketAccountRepository.save(transferFromAccount);
            } catch (DataIntegrityViolationException e) {

            }
        }
        logger.info("red packet record end");
        return "success";
    }

    /**
     * confirm阶段，
     * 1.更新交易记录confirm状态，
     * 2.给收款方增加红包余额（可能作者认为，这是红包服务整个操作流程的最后一环）
     *
     * @param transactionContext
     * @param tradeOrderDto
     */
    @Transactional
    public void confirmRecord(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("red packet confirm record called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        if (null != tradeOrder && "DRAFT".equals(tradeOrder.getStatus())) {
            logger.info("根据dto传来的订单号，查询到交易记录{}，开始confirm，更新交易记录状态为confirm", tradeOrder);
            tradeOrder.confirm();
            tradeOrderRepository.update(tradeOrder);
            //确认阶段，进行下一步确认，try阶段，已经扣减了红包，这里的确认，是将扣减的红包，加到收款方的红包账户
            RedPacketAccount transferToAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getOppositeUserId());
            //这里作者认为收款方（商家平台）也有一个红包账户，如果进入到了confirm阶段，将红包金额加入商家红包账户
            transferToAccount.transferTo(tradeOrderDto.getAmount());
            logger.info("查询到红包信息{}，增加商家红包余额，save红包信息", transferToAccount);

            redPacketAccountRepository.save(transferToAccount);
        }
        int x = 1 / 0;
        logger.info("redpacket confirm end");
    }

    /**
     * cancle阶段，回退try阶段扣除的红包
     *
     * @param transactionContext
     * @param tradeOrderDto
     */
    @Transactional
    public void cancelRecord(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("red packet cancel record called. time seq:" + DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd HH:mm:ss"));

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        if (null != tradeOrder && "DRAFT".equals(tradeOrder.getStatus())) {
            logger.info("redpacket 根据dto 订单号查询交易记录{}，并确认其状态为DRAFT（confirm失败）", tradeOrder);

            tradeOrder.cancel();
            tradeOrderRepository.update(tradeOrder);

            RedPacketAccount capitalAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());
            logger.info("补偿之前，红包信息{}", capitalAccount);
            //此处，将try阶段扣减的红包退回。
            capitalAccount.cancelTransfer(tradeOrderDto.getAmount());
            logger.info("回退红包金额{}，更新到db", capitalAccount);
            redPacketAccountRepository.save(capitalAccount);
        }
    }
}

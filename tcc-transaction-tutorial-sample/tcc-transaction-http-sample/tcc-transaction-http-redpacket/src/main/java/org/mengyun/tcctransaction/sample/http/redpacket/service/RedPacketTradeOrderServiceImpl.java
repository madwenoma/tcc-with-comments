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
                logger.info("交易记录为空，新生成交易记录{}，开始insert交易记录", tradeOrder);
                tradeOrderRepository.insert(tradeOrder);

                RedPacketAccount transferFromAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());

                transferFromAccount.transferFrom(tradeOrderDto.getAmount());
                logger.info("根据dto传来的userid，查询到用户账户{}，开始更新账户信息", transferFromAccount);

                redPacketAccountRepository.save(transferFromAccount);
            } catch (DataIntegrityViolationException e) {

            }
        }
        logger.info("red packet record end");
        return "success";
    }

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

            RedPacketAccount transferToAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getOppositeUserId());

            transferToAccount.transferTo(tradeOrderDto.getAmount());
            logger.info("查询到红包信息{}，准备扣红包，save红包信息", transferToAccount);

            redPacketAccountRepository.save(transferToAccount);
        }
        logger.info("redpacket confirm end");
    }

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
            capitalAccount.cancelTransfer(tradeOrderDto.getAmount());
            logger.info("回退红包金额{}，更新到db", capitalAccount);
            redPacketAccountRepository.save(capitalAccount);
        }
    }
}

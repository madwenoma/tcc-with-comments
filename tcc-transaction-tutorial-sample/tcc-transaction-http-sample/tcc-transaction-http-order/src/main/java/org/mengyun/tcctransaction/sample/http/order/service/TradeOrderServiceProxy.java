package org.mengyun.tcctransaction.sample.http.order.service;

import javafx.application.Application;
import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.Propagation;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.context.MethodTransactionContextEditor;
import org.mengyun.tcctransaction.sample.http.capital.api.CapitalTradeOrderService;
import org.mengyun.tcctransaction.sample.http.capital.api.dto.CapitalTradeOrderDto;
import org.mengyun.tcctransaction.sample.http.redpacket.api.RedPacketTradeOrderService;
import org.mengyun.tcctransaction.sample.http.redpacket.api.dto.RedPacketTradeOrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by changming.xie on 4/19/17.
 */
@Component
public class TradeOrderServiceProxy {
    private static int init = 1;

    public TradeOrderServiceProxy() {
        System.out.println(init++);
        System.out.println(this);
    }

    @Autowired
    CapitalTradeOrderService capitalTradeOrderService;

    @Autowired
    RedPacketTradeOrderService redPacketTradeOrderService;

    /*the propagation need set Propagation.SUPPORTS,otherwise the recover doesn't work,
      The default value is Propagation.REQUIRED, which means will begin new transaction when recover.
    */
    @Compensable(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
    public String record(TransactionContext transactionContext, CapitalTradeOrderDto tradeOrderDto) {
        System.out.println("TradeOrderServiceProxy capitalTradeOrderService record...");

        return capitalTradeOrderService.record(transactionContext, tradeOrderDto);
    }

    @Compensable(propagation = Propagation.SUPPORTS, confirmMethod = "record", cancelMethod = "record", transactionContextEditor = MethodTransactionContextEditor.class)
    public String record(TransactionContext transactionContext, RedPacketTradeOrderDto tradeOrderDto) {
        System.out.println("TradeOrderServiceProxy redPacketTradeOrderService record...");
        return redPacketTradeOrderService.record(transactionContext, tradeOrderDto);
    }
}

package org.mengyun.tcctransaction.recover;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mengyun.tcctransaction.OptimisticLockException;
import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.TransactionRepository;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.common.TransactionType;
import org.mengyun.tcctransaction.support.TransactionConfigurator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by changmingxie on 11/10/15.
 * 事务恢复逻辑
 */
public class TransactionRecovery {

    static final Logger logger = Logger.getLogger(TransactionRecovery.class.getSimpleName());

    private TransactionConfigurator transactionConfigurator;

    //是通过RecoverScheduledJob里obDetail.setTargetMethod("startRecover");调用
    public void startRecover() {

        List<Transaction> transactions = loadErrorTransactions();

        recoverErrorTransactions(transactions);
    }

    private List<Transaction> loadErrorTransactions() {


        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();

        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        RecoverConfig recoverConfig = transactionConfigurator.getRecoverConfig();
        //配置的duration是5秒，那就找出5秒之前进行过更新的所有事务
        //TODO  为什么这些事务是error transaction
        //存在，未被删除，就是需要处理?
        return transactionRepository.findAllUnmodifiedSince(new Date(currentTimeInMillis - recoverConfig.getRecoverDuration() * 1000));
    }

    private void recoverErrorTransactions(List<Transaction> transactions) {
        logger.info("recoverErrorTransactions transactions " + new Date());

        for (Transaction transaction : transactions) {
            //超过最大重试次数 不再尝试恢复，抛出异常需人工恢复
            int maxRetryCount = transactionConfigurator.getRecoverConfig().getMaxRetryCount();
            if (transaction.getRetriedCount() > maxRetryCount) {

                logger.error(String.format("recover failed with max retry count,will not try again. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)));
                continue;
            }

            //分支事务超时
            int recoverDuration = transactionConfigurator.getRecoverConfig().getRecoverDuration();

            if (transaction.getTransactionType().equals(TransactionType.BRANCH)
                    && (transaction.getCreateTime().getTime() + maxRetryCount * recoverDuration * 1000 > System.currentTimeMillis())) { //TODO 事务尝试还未超时？不到恢复的时间？
                continue;
            }

            try {
                transaction.addRetriedCount();
                //当前事务confirming阶段 尝试提交
                if (transaction.getStatus().equals(TransactionStatus.CONFIRMING)) {

                    transaction.changeStatus(TransactionStatus.CONFIRMING);
                    transactionConfigurator.getTransactionRepository().update(transaction);
                    logger.info("recoverErrorTransactions commit...");
                    transaction.commit();
                    transactionConfigurator.getTransactionRepository().delete(transaction);
                } else if (transaction.getStatus().equals(TransactionStatus.CANCELLING)
                        || transaction.getTransactionType().equals(TransactionType.ROOT)) {
                    //TODO ROOT事务为什么要rollabck？延迟失败处理？在main线程里断点rollback，job线程，就会进入此处进行rollback
                    logger.info("recoverErrorTransactions rollback..");
                    logger.info("transaction status is " + transaction.getStatus());
                    logger.info("transaction type is " + transaction.getTransactionType());
                    transaction.changeStatus(TransactionStatus.CANCELLING);
                    transactionConfigurator.getTransactionRepository().update(transaction);
                    transaction.rollback();
                    transactionConfigurator.getTransactionRepository().delete(transaction);//事务正常完毕，是不是都是会delete掉，存在的都是有问题的
                }

            } catch (Throwable throwable) {

                if (throwable instanceof OptimisticLockException
                        || ExceptionUtils.getRootCause(throwable) instanceof OptimisticLockException) {
                    logger.warn(String.format("optimisticLockException happened while recover. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)), throwable);
                } else {
                    logger.error(String.format("recover failed, txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)), throwable);
                }
            }
        }
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}

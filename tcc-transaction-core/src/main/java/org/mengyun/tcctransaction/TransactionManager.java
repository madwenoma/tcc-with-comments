package org.mengyun.tcctransaction;

import org.apache.log4j.Logger;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.common.TransactionType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

/**
 * Created by changmingxie on 10/26/15.
 */
public class TransactionManager {

    static final Logger logger = Logger.getLogger(TransactionManager.class.getSimpleName());

    private TransactionRepository transactionRepository;

    private static final ThreadLocal<Deque<Transaction>> CURRENT = new ThreadLocal<Deque<Transaction>>();

    private ExecutorService executorService;

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public TransactionManager() {
    }

    /**
     * 开始事务，说明这是一个根事务
     * TransactionType.ROOT
     *
     * @return
     */
    public Transaction begin() {
        logger.info("开启一个根事务");
        Transaction transaction = new Transaction(TransactionType.ROOT);
        transactionRepository.create(transaction);
        registerTransaction(transaction);
        return transaction;
    }

    /**
     * 传播新事务
     *
     * @param transactionContext
     * @return
     */
    public Transaction propagationNewBegin(TransactionContext transactionContext) {
        logger.info("传播新事务begin," + transactionContext);
        Transaction transaction = new Transaction(transactionContext);//new出来的transaction
        transactionRepository.create(transaction);
        logger.info("创建Transaction " + transaction);
        registerTransaction(transaction);
        return transaction;
    }

    /**
     * 根据事务上下文xid，找到已存在的事务（通常用于confirm阶段，找到try阶段创建的事务）
     *
     * @param transactionContext
     * @return
     * @throws NoExistedTransactionException
     */
    public Transaction propagationExistBegin(TransactionContext transactionContext) throws NoExistedTransactionException {
        logger.info("传播已存在事务begin," + transactionContext);

        Transaction transaction = transactionRepository.findByXid(transactionContext.getXid());
        logger.info("根据xid查找到的Transaction： " + transaction);

        if (transaction != null) {
            transaction.changeStatus(TransactionStatus.valueOf(transactionContext.getStatus()));//更改事务状态，如trying改为confirming，confirming改为cancling
            registerTransaction(transaction);
            return transaction;
        } else {
            throw new NoExistedTransactionException();
        }
    }

    /**
     * 进行提交
     *
     * @param asyncCommit 是否异步提交
     */
    public void commit(boolean asyncCommit) {

        final Transaction transaction = getCurrentTransaction();

        transaction.changeStatus(TransactionStatus.CONFIRMING);

        transactionRepository.update(transaction);

        if (asyncCommit) {
            try {
                Long statTime = System.currentTimeMillis();

                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        commitTransaction(transaction);
                    }
                });
                logger.debug("async submit cost time:" + (System.currentTimeMillis() - statTime));
            } catch (Throwable commitException) {
                logger.warn("compensable transaction async submit confirm failed, recovery job will try to confirm later.", commitException);
                throw new ConfirmingException(commitException);
            }
        } else {
            commitTransaction(transaction);
        }
    }

    public void syncTransaction() {

        final Transaction transaction = getCurrentTransaction();
        /**
         * update the transaction to persist the participant context info
         */
        transactionRepository.update(transaction);
    }

    public void rollback(boolean asyncRollback) {
        final Transaction transaction = getCurrentTransaction();
        logger.info("rollback ,current transaction is ");
        transaction.changeStatus(TransactionStatus.CANCELLING);
        logger.info("rollback ,change status to canceling ");

        transactionRepository.update(transaction);
        logger.info("rollback , update redis over");

        if (asyncRollback) {

            try {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        rollbackTransaction(transaction);
                    }
                });
            } catch (Throwable rollbackException) {
                logger.warn("compensable transaction async rollback failed, recovery job will try to rollback later.", rollbackException);
                throw new CancellingException(rollbackException);
            }
        } else {

            rollbackTransaction(transaction);
        }
    }


    private void commitTransaction(Transaction transaction) {
        try {
            transaction.commit();
            transactionRepository.delete(transaction);
        } catch (Throwable commitException) {
            logger.warn("compensable transaction confirm failed, recovery job will try to confirm later.", commitException);
            throw new ConfirmingException(commitException);
        }
    }

    private void rollbackTransaction(Transaction transaction) {
        try {
            transaction.rollback();
            transactionRepository.delete(transaction);
            logger.info("delete redis over");
        } catch (Throwable rollbackException) {
            logger.warn("compensable transaction rollback failed, recovery job will try to rollback later.", rollbackException);
            throw new CancellingException(rollbackException);
        }
    }

    public Transaction getCurrentTransaction() {
        if (isTransactionActive()) {
            return CURRENT.get().peek();
        }
        return null;
    }

    /**
     * 当前有事务
     * @return
     */
    public boolean isTransactionActive() {
        Deque<Transaction> transactions = CURRENT.get();
        return transactions != null && !transactions.isEmpty();
    }


    private void registerTransaction(Transaction transaction) {

        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<Transaction>());
        }
        logger.info("registerTransaction LinkedList size:" + CURRENT.get().size());
        CURRENT.get().push(transaction);
    }

    public void cleanAfterCompletion(Transaction transaction) {
//        System.out.println("cleanAfterCompletion LinkedList size:" + CURRENT.get().size());
        if (isTransactionActive() && transaction != null) {
            Transaction currentTransaction = getCurrentTransaction();
            if (currentTransaction == transaction) {
                CURRENT.get().pop();
            } else {
                throw new SystemException("Illegal transaction when clean after completion");
            }
        }
    }


    public void enlistParticipant(Participant participant) {
        Transaction transaction = this.getCurrentTransaction();
        transaction.enlistParticipant(participant);
        transactionRepository.update(transaction);
    }
}

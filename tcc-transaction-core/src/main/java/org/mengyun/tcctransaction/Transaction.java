package org.mengyun.tcctransaction;


import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.api.TransactionXid;
import org.mengyun.tcctransaction.common.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by changmingxie on 10/26/15.
 */
public class Transaction implements Serializable {
    private Logger logger = LoggerFactory.getLogger(Transaction.class);
    private static final long serialVersionUID = 7291423944314337931L;

    private TransactionXid xid;

    private TransactionStatus status;

    private TransactionType transactionType;

    private volatile int retriedCount = 0;

    private Date createTime = new Date();

    private Date lastUpdateTime = new Date();

    private long version = 1;

    private List<Participant> participants = new ArrayList<Participant>();

    private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();

    public Transaction() {

    }

    /**
     * 根据事务上下文new出来的事务，说明是一个分支事务
     * xid是上下文的xid（root事务的xid？）
     *
     * @param transactionContext
     */
    public Transaction(TransactionContext transactionContext) {
        this.xid = transactionContext.getXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = TransactionType.BRANCH;
    }

    /**
     * 根据type创建一个事务 xid为新生的
     *
     * @param transactionType
     */
    public Transaction(TransactionType transactionType) {
        this.xid = new TransactionXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = transactionType;
    }

    public void enlistParticipant(Participant participant) {
        participants.add(participant);
    }


    public Xid getXid() {
        return xid.clone();
    }

    public TransactionStatus getStatus() {
        return status;
    }


    public List<Participant> getParticipants() {
        return participants;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void changeStatus(TransactionStatus status) {
        this.status = status;
    }


    public void commit() {
//        logger.info("Transaction commit,participants size is " + participants.size());
        for (Participant participant : participants) {
//            logger.info("participant info:{}" + participant);
            participant.commit();
        }
    }

    public void rollback() {
//        logger.info("rolback,participants size is {}" + participants == null ? 0 : participants.size());
        logger.info("Transaction rollback...");
        for (Participant participant : participants) {
            logger.info("participant cancel-method:" + participant.getCancelInvocationContext());
            participant.rollback();
        }
    }

    public int getRetriedCount() {
        return retriedCount;
    }

    public void addRetriedCount() {
        this.retriedCount++;
    }

    public void resetRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public Map<String, Object> getAttachments() {
        return attachments;
    }

    public long getVersion() {
        return version;
    }

    public void updateVersion() {
        this.version++;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date date) {
        this.lastUpdateTime = date;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void updateTime() {
        this.lastUpdateTime = new Date();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "xid=" + xid +
                ", status=" + status +
                ", transactionType=" + transactionType +
                ", retriedCount=" + retriedCount +
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                ", version=" + version +
                ", participants=" + participants +
                ", attachments=" + attachments +
                '}';
    }

    public String toSimpleString() {
        return "Transaction{" +
                "xid=" + xid +
                ", status=" + status +
                ", transactionType=" + transactionType +
                ", createTime=" + createTime +
                ", lastUpdateTime=" + lastUpdateTime +
                '}';
    }
}

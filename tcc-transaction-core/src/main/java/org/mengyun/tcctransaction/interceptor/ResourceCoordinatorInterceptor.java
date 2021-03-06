package org.mengyun.tcctransaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mengyun.tcctransaction.InvocationContext;
import org.mengyun.tcctransaction.Participant;
import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.TransactionManager;
import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.api.TransactionXid;
import org.mengyun.tcctransaction.support.FactoryBuilder;
import org.mengyun.tcctransaction.utils.CompensableMethodUtils;
import org.mengyun.tcctransaction.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 11/8/15.
 * 在 Try 阶段，添加参与者到事务中。当事务上下文不存在时，进行创建Participant
 * 这是协调者拦截器的用处，该拦截器执行是在可补偿拦截器之后，定义了Order顺序是+1的：Ordered.HIGHEST_PRECEDENCE + 1;
 */
public class ResourceCoordinatorInterceptor {

    Logger logger = LoggerFactory.getLogger(ResourceCoordinatorInterceptor.class);
    private TransactionManager transactionManager;

    //configuration里new了manager并set到这里
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {

        Transaction transaction = transactionManager.getCurrentTransaction();

        if (transaction != null) {

            switch (transaction.getStatus()) {
                case TRYING:
                    enlistParticipant(pjp);
                    break;
                case CONFIRMING:
                    break;
                case CANCELLING:
                    break;
            }
        }

        return pjp.proceed(pjp.getArgs());
    }

    private void enlistParticipant(ProceedingJoinPoint pjp) throws IllegalAccessException, InstantiationException {

        Method method = CompensableMethodUtils.getCompensableMethod(pjp);
        if (method == null) {
            throw new RuntimeException(String.format("join point not found method, point is : %s", pjp.getSignature().getName()));
        }
        Compensable compensable = method.getAnnotation(Compensable.class);

        String confirmMethodName = compensable.confirmMethod();
        String cancelMethodName = compensable.cancelMethod();

        Transaction transaction = transactionManager.getCurrentTransaction();
        //生成参与者子事务id，参与者都是子事务（包含本地和远程），所以xid都是新生成。
        //如果代码在order 事务发起段执行，生成的远程子事务id，会通过事务上线文（TransactionContext）传给远端服务
        //如果代码在redpacket和capital项目执行，这里生成的子事务xid似乎没什么用
        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());

        if (FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().get(pjp.getTarget(), method, pjp.getArgs()) == null) {
            FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().
                    set(new TransactionContext(xid, TransactionStatus.TRYING.getId()),
                            pjp.getTarget(), ((MethodSignature) pjp.getSignature()).getMethod(), pjp.getArgs());
        }

        Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        InvocationContext confirmInvocation =
                new InvocationContext(targetClass, confirmMethodName, method.getParameterTypes(), pjp.getArgs());

        InvocationContext cancelInvocation =
                new InvocationContext(targetClass, cancelMethodName, method.getParameterTypes(), pjp.getArgs());
        //这里封装成参与者Participant
        //如果是order项目，事务发起者，commit或callback阶段，会对3个参与者
        Participant participant =
                new Participant(xid, confirmInvocation, cancelInvocation, compensable.transactionContextEditor());
        logger.info(participant.toString());
        transactionManager.enlistParticipant(participant);

    }


}

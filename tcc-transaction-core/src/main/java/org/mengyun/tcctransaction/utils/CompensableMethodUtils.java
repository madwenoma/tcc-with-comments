package org.mengyun.tcctransaction.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.Propagation;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.common.MethodType;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 11/21/15.
 */
public class CompensableMethodUtils {

    public static Method getCompensableMethod(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

        if (method.getAnnotation(Compensable.class) == null) {
            try {
                method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return method;
    }

    public static MethodType calculateMethodType(Propagation propagation, boolean isTransactionActive, TransactionContext transactionContext) {
        //REQUIRED且当前threadlocal里有事务且事务上下文不为空
        //或 事务隔离级别为REQUIRES_NEW
        //以上两种是root事务
        if ((propagation.equals(Propagation.REQUIRED) && !isTransactionActive && transactionContext == null) ||
                propagation.equals(Propagation.REQUIRES_NEW)) {
            return MethodType.ROOT;
        } else if ((propagation.equals(Propagation.REQUIRED) || propagation.equals(Propagation.MANDATORY)) && !isTransactionActive && transactionContext != null) {
            //REQUIRED或MANDATORY隔离级别，且当前threadlocal里有事务且事务上下文不为空
            return MethodType.PROVIDER;
        } else {
            //其他返回NORMAL TODO 其他都包含哪些情况
            return MethodType.NORMAL;
        }
    }

    public static MethodType calculateMethodType(TransactionContext transactionContext, boolean isCompensable) {

        if (transactionContext == null && isCompensable) {
            //isRootTransactionMethod
            return MethodType.ROOT;
        } else if (transactionContext == null && !isCompensable) {
            //isSoaConsumer
            return MethodType.CONSUMER;
        } else if (transactionContext != null && isCompensable) {
            //isSoaProvider
            return MethodType.PROVIDER;
        } else {
            return MethodType.NORMAL;
        }
    }

    public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {

        int position = -1;

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(org.mengyun.tcctransaction.api.TransactionContext.class)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static TransactionContext getTransactionContextFromArgs(Object[] args) {

        TransactionContext transactionContext = null;

        for (Object arg : args) {
            if (arg != null && org.mengyun.tcctransaction.api.TransactionContext.class.isAssignableFrom(arg.getClass())) {

                transactionContext = (org.mengyun.tcctransaction.api.TransactionContext) arg;
            }
        }

        return transactionContext;
    }
}

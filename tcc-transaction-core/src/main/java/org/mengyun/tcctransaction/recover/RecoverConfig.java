package org.mengyun.tcctransaction.recover;

import java.util.Set;

/**
 * Created by changming.xie on 6/1/16.
 */
public interface RecoverConfig {

    public int getMaxRetryCount();

    //恢复以当前为基准，该值之前做过更新的事务
    //当前10:00 ，该值设置为1分钟
    //事务会对09:59之前有过更新的事务进行恢复
    //如果设置过短，main逻辑对事务进行恢复的时候，recover job也会立即恢复
    //http demo里order的recover duration是5，redpacket是60，相差较大，说明order项目是主要的恢复角色
    //无论是恢复持续时间，还是job执行间隔，子项目都是备用角色， 执行间隔长，60s之前update的·事务才会被恢复
    //order项目执行间隔短，恢复时间短（反应快，5s之前update过的事务就会恢复）
    public int getRecoverDuration();

    //执行时间表达式
    //order间隔5s，子项目间隔30s，子项目只保证自己项目进行恢复
    public String getCronExpression();

    public Set<Class<? extends Exception>> getDelayCancelExceptions();

    public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayRecoverExceptions);

    public int getAsyncTerminateThreadPoolSize();
}

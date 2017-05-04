package cn.newtouch.drpc;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/5/4.
 */
public abstract class DrpcInvoker<T extends DrpcSession> {

    protected abstract Object invoke(T session, Method method, Object[] args) throws Exception;

}

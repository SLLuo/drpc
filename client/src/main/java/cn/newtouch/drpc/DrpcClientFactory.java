package cn.newtouch.drpc;

/**
 * Created by Administrator on 2017/5/5.
 */
public interface DrpcClientFactory<T extends DrpcSession> {

    Object makeClient(T session) throws Throwable;

}

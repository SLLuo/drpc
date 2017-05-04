package cn.newtouch.drpc;

/**
 * Created by Administrator on 2017/5/4.
 */
public abstract class DrpcSession {

    public abstract void open() throws Exception;

    public abstract boolean validate();

    public abstract void close() throws Exception;

}

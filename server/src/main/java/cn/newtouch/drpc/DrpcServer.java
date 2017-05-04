package cn.newtouch.drpc;

/**
 * Created by Administrator on 2017/5/3.
 */
public abstract class DrpcServer {

    private int port;

    public abstract boolean isServing();

    public abstract void startup() throws Exception;

    public abstract void shutdown() throws Exception;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

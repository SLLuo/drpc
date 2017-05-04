package cn.newtouch.drpc;

/**
 * Created by Administrator on 2017/5/3.
 */
public abstract class DrpcEnroller {

    private String namespace;

    private String address;

    public abstract void startup() throws Exception;

    public abstract void shutdown() throws Exception;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

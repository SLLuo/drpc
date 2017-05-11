package cn.newtouch.drpc.thrift;

import cn.newtouch.drpc.DrpcSessionFactory;
import com.netflix.loadbalancer.Server;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Created by Administrator on 2017/5/4.
 */
public class ThriftDrpcSessionFactory extends DrpcSessionFactory<ThriftDrpcSession> {

    private int connectTimeout = 100;
    private int responseTimeout = 1000;

    public ThriftDrpcSession makeObject(Server server) throws Exception {
        TTransport transport = new TSocket(server.getHost(), server.getPort(), responseTimeout, connectTimeout);
        ThriftDrpcSession session = new ThriftDrpcSession(transport);
        session.open();
        return session;
    }

    public void destroyObject(Server server, ThriftDrpcSession session) throws Exception {
        session.close();
    }

    public boolean validateObject(Server server, ThriftDrpcSession session) {
        return session.validate();
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }
}

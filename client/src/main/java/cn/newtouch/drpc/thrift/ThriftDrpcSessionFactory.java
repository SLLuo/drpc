package cn.newtouch.drpc.thrift;

import cn.newtouch.drpc.DrpcSessionFactory;
import com.netflix.loadbalancer.Server;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Created by Administrator on 2017/5/4.
 */
public class ThriftDrpcSessionFactory extends DrpcSessionFactory<ThriftDrpcSession> {

    private int timeout = 60;

    public ThriftDrpcSession makeObject(Server server) throws Exception {
        TTransport transport = new TSocket(server.getHost(), server.getPort(), timeout);
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

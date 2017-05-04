package cn.newtouch.drpc.thrift;

import cn.newtouch.drpc.DrpcFactory;
import cn.newtouch.drpc.DrpcInvoker;
import cn.newtouch.drpc.DrpcSession;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/5/4.
 */
public class ThriftDrpcInvoker extends DrpcInvoker<ThriftDrpcSession> {

    private String serviceName;
    private TServiceClientFactory serviceClientFactory;

    @Override
    protected Object invoke(ThriftDrpcSession session, Method method, Object[] args) throws Exception {
        TBinaryProtocol protocol = new TBinaryProtocol(session.getTransport());
        TMultiplexedProtocol mProtocol = new TMultiplexedProtocol(protocol, serviceName);
        return method.invoke(serviceClientFactory.getClient(mProtocol), args);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public TServiceClientFactory getServiceClientFactory() {
        return serviceClientFactory;
    }

    public void setServiceClientFactory(TServiceClientFactory serviceClientFactory) {
        this.serviceClientFactory = serviceClientFactory;
    }
}

package cn.newtouch.drpc.thrift;

import cn.newtouch.drpc.DrpcClientFactory;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;

/**
 * Created by Administrator on 2017/5/5.
 */
public class ThriftDrpcClientFactory implements DrpcClientFactory<ThriftDrpcSession> {

    private String serviceName;
    private TServiceClientFactory serviceClientFactory;

    @Override
    public Object makeClient(ThriftDrpcSession session) throws Throwable {
        TBinaryProtocol protocol = new TBinaryProtocol(session.getTransport());
        TMultiplexedProtocol mProtocol = new TMultiplexedProtocol(protocol, serviceName);
        return serviceClientFactory.getClient(mProtocol);
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

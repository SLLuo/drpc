package cn.newtouch.drpc.thrift;

import cn.newtouch.drpc.DrpcFactory;
import cn.newtouch.drpc.DrpcSession;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TTransport;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/5/4.
 */
public class ThriftDrpcSession extends DrpcSession {

    private final TTransport transport;

    public ThriftDrpcSession(TTransport transport) {
        this.transport = transport;
    }

    @Override
    public void open() throws Exception {
        transport.open();
    }

    @Override
    public boolean validate() {
        return transport.isOpen();
    }

    @Override
    public void close() throws Exception {
        transport.close();
    }

    public TTransport getTransport() {
        return transport;
    }
}

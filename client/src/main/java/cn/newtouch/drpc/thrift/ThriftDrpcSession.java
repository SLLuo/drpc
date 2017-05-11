package cn.newtouch.drpc.thrift;

import cn.newtouch.drpc.DrpcSession;
import org.apache.thrift.transport.TTransport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/4.
 */
public class ThriftDrpcSession extends DrpcSession {

    private final TTransport transport;
    private Map<String, Object> clientMap;

    public ThriftDrpcSession(TTransport transport) {
        this.transport = transport;
        clientMap = new HashMap<>();
    }

    public Object getClient(String name) {
        return clientMap.get(name);
    }

    public void setClient(String name, Object client) {
        clientMap.put(name, client);
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
        if (transport.isOpen()) {
            transport.close();
        }
    }

    public TTransport getTransport() {
        return transport;
    }
}

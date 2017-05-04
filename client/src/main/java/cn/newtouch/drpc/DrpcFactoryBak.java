package cn.newtouch.drpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class DrpcFactoryBak extends BasePoolableObjectFactory<TServiceClient> {
    private int timeout;

    public static class Config {
        public String ip;
        public int port;

        public Config(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    }

    private int idx = 0;
    private String serviceName;
    private TServiceClientFactory serviceClientFactory;
    private Config[] configs = new Config[0];
    private Map<String, Config> configMap = new ConcurrentHashMap();

    public DrpcFactoryBak(String serviceName, TServiceClientFactory serviceClientFactory, int timeout) {
        this.timeout = timeout;
        this.serviceName = serviceName;
        this.serviceClientFactory = serviceClientFactory;
    }

    public void addConfig(String path, Config config) {
        this.configMap.put(path, config);
        this.configs = this.configMap.values().toArray(new Config[this.configMap.size()]);
        synchronized (this.configMap) {
            this.configMap.notifyAll();
        }
    }

    public Config removeConfig(String path) {
        Config config = this.configMap.remove(path);
        this.configs = this.configMap.values().toArray(new Config[this.configMap.size()]);
        synchronized (this.configMap) {
            this.configMap.notifyAll();
        }
        return config;
    }

    public Config getConfig() throws Exception {
        if (this.configs.length == 0) {
            synchronized (this.configMap) {
                this.configMap.wait(10000L);
                if (this.configs.length == 0) {
                    throw new RuntimeException("no server!");
                }
            }
        }
        Config config = this.configs[(this.idx++)];
        if (this.idx >= this.configs.length) {
            this.idx = 0;
        }
        return config;
    }

    public TServiceClient makeObject() throws Exception {
        Config config = getConfig();
        TTransport transport = new TSocket(config.ip, config.port, this.timeout);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        TMultiplexedProtocol mProtocol = new TMultiplexedProtocol(protocol, this.serviceName);
        TServiceClient client = this.serviceClientFactory.getClient(mProtocol);
        transport.open();
        return client;
    }

    public void destroyObject(TServiceClient client) throws Exception {
        client.getInputProtocol().getTransport().close();
    }

    public boolean validateObject(TServiceClient client) {
        return client.getInputProtocol().getTransport().isOpen();
    }
}

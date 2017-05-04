package cn.newtouch.drpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.TServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DrpcDetectorBak implements InitializingBean, PathChildrenCacheListener {
    public static final Logger LOGGER = LoggerFactory.getLogger(DrpcDetectorBak.class);
    private boolean running = false;
    private int timeout = 600000;
    private Integer maxActive = Integer.valueOf(32);
    private Integer idleTime = Integer.valueOf(180000);
    private String name;
    private String zkURIs;
    private CuratorFramework zookeeper;
    private PathChildrenCache pathChildrenCache;
    private Map<String, String> nameServiceClassNameMap;
    private Map<String, Class<?>> nameServiceIfaceClassMap;
    private Map<String, TServiceClientFactory> nameServiceClientFactoryMap;
    private Map<String, DrpcFactoryBak> namePoolServiceFactoryMap = new ConcurrentHashMap();
    private Map<String, GenericObjectPool> namePoolMap = new ConcurrentHashMap();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public void setIdleTime(Integer idleTime) {
        this.idleTime = idleTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setZkURIs(String zkURIs) {
        this.zkURIs = zkURIs;
    }

    public void setNameServiceClassNameMap(Map<String, String> nameServiceClassNameMap) throws Exception {
        this.nameServiceIfaceClassMap = new HashMap();
        this.nameServiceClientFactoryMap = new HashMap();
        for (Map.Entry<String, String> entry : nameServiceClassNameMap.entrySet()) {
            this.nameServiceIfaceClassMap.put(entry.getKey(), this.classLoader.loadClass(entry.getValue() + "$Iface"));
            this.nameServiceClientFactoryMap.put(entry.getKey(),
                    (TServiceClientFactory) this.classLoader.loadClass(entry.getValue() + "$Client$Factory").newInstance());
        }
    }

    public void createPoolServiceFactory() throws Exception {
        for (Map.Entry<String, TServiceClientFactory> entry : this.nameServiceClientFactoryMap.entrySet()) {
            GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
            poolConfig.maxActive = this.maxActive.intValue();
            poolConfig.minIdle = 0;
            poolConfig.testOnBorrow = true;
            poolConfig.testWhileIdle = true;
            poolConfig.minEvictableIdleTimeMillis = this.idleTime.intValue();
            poolConfig.timeBetweenEvictionRunsMillis = (this.idleTime.intValue() / 2L);
            DrpcFactoryBak factory = new DrpcFactoryBak(entry.getKey(), entry.getValue(), this.timeout);
            this.namePoolServiceFactoryMap.put(entry.getKey(), factory);
            GenericObjectPool<TServiceClient> pool = new GenericObjectPool(factory, poolConfig);
            this.namePoolMap.put(entry.getKey(), pool);
        }
    }

    private void createClient(String path, String ip, int port) {
        for (Map.Entry<String, DrpcFactoryBak> entry : this.namePoolServiceFactoryMap.entrySet()) {
            (entry.getValue()).addConfig(path, new DrpcFactoryBak.Config(ip, port));
        }
    }

    private void addClient(String path, String addrs) {
        String[] addrArr = addrs.split(";");
        for (String addr : addrArr) {
            String[] ipAndPort = addr.split(":");
            createClient(path, ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        }
    }

    private void removeClient(String path) {
        for (Map.Entry<String, DrpcFactoryBak> entry : this.namePoolServiceFactoryMap.entrySet()) {
            entry.getValue().removeConfig(entry.getKey());
        }
    }

    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        Type eventType = event.getType();
        LOGGER.info(eventType.name(), event);
        switch (eventType) {
            case CHILD_ADDED:
                ChildData data1 = event.getData();
                addClient(data1.getPath(), new String(data1.getData(), "UTF-8"));
                break;
            case CHILD_REMOVED:
                ChildData data2 = event.getData();
                removeClient(data2.getPath());
                break;
        }
    }

    private void startZookeeper() throws Exception {
        this.zookeeper = CuratorFrameworkFactory.builder()
                .connectString(this.zkURIs)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .canBeReadOnly(true)
                .namespace("drpc")
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .defaultData(null)
                .build();
        this.zookeeper.start();
        this.pathChildrenCache = new PathChildrenCache(this.zookeeper, this.name, true);
        this.pathChildrenCache.getListenable().addListener(this);
        this.pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        LOGGER.info("zookeeper start!");
    }

    public Object getBean(String name) {
        final GenericObjectPool<TServiceClient> pool = (GenericObjectPool) this.namePoolMap.get(name);
        Class<?> serviceClass = this.nameServiceIfaceClassMap.get(name);
        return Proxy.newProxyInstance(this.classLoader, new Class[]{serviceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                TServiceClient client = pool.borrowObject();
                try {
                    return method.invoke(client, args);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                } finally {
                    pool.returnObject(client);
                }
            }
        });
    }

    public void afterPropertiesSet() throws Exception {
        this.running = true;
        createPoolServiceFactory();
        startZookeeper();
    }

    public void stop() {
        if (this.running) {
            this.running = false;
            try {
                this.pathChildrenCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.zookeeper.close();
            for (GenericObjectPool pool : this.namePoolMap.values()) {
                try {
                    pool.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

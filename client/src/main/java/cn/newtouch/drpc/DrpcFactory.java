package cn.newtouch.drpc;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/5/4.
 */
public class DrpcFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(DrpcFactory.class);

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private int maxActive = 32;
    private int idleTime = 180000;
    private Class<?> serviceClass;

    private DrpcInvoker invoker;
    private DrpcCluster cluster;
    private DrpcSessionFactory<DrpcSession> sessionFactory;

    private GenericKeyedObjectPool<Server, DrpcSession> sessionPool;
    private ILoadBalancer loadBalancer;
    private LoadBalancerCommand<Object> loadBalancerCommand;

    public void startup() throws Exception {
        cluster.startup();
        Config config = new Config();
        config.maxActive = maxActive;
        config.minIdle = 0;
        config.testOnBorrow = true;
        config.testWhileIdle = true;
        config.minEvictableIdleTimeMillis = idleTime;
        config.timeBetweenEvictionRunsMillis = (idleTime / 2L);
        sessionPool = new GenericKeyedObjectPool(sessionFactory, config);
        IClientConfig clientConfig = new DefaultClientConfigImpl();
        clientConfig.set(CommonClientConfigKey.MaxAutoRetries, 3);
        loadBalancer = LoadBalancerBuilder.newBuilder()
                .withDynamicServerList(cluster)
                .withRule(new RoundRobinRule())
                .withClientConfig(clientConfig)
                .buildDynamicServerListLoadBalancer();
        loadBalancerCommand = LoadBalancerCommand.builder()
                .withRetryHandler(new DefaultLoadBalancerRetryHandler())
                .withLoadBalancer(loadBalancer).build();
    }

    public void shutdown() throws Exception {
        cluster.shutdown();
        sessionPool.close();
    }

    private class DrpcServerOperation implements ServerOperation<Object> {
        private Method method;
        private Object[] args;

        public DrpcServerOperation(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        @Override
        public Observable<Object> call(Server server) {
            DrpcSession session = null;
            try {
                session = sessionPool.borrowObject(server);
                return Observable.just(invoker.invoke(session, method, args));
            } catch (Exception e) {
                return Observable.error(e);
            } finally {
                if (session != null)
                    try {
                        sessionPool.returnObject(server, session);
                    } catch (Exception e) {
                        LOGGER.debug("", e);
                    }
            }
        }
    }

    public Object proxyClient() throws Exception {
        return Proxy.newProxyInstance(this.classLoader, new Class[]{serviceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                Object result = loadBalancerCommand.submit(new DrpcServerOperation(method, args)).toBlocking().single();
                System.out.println(result.getClass());
                if (result instanceof Throwable) throw (Throwable) result;
                else return result;
            }
        });
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public DrpcInvoker getInvoker() {
        return invoker;
    }

    public void setInvoker(DrpcInvoker invoker) {
        this.invoker = invoker;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public DrpcCluster getCluster() {
        return cluster;
    }

    public void setCluster(DrpcCluster cluster) {
        this.cluster = cluster;
    }

    public DrpcSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(DrpcSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}

package cn.newtouch.drpc;

import com.netflix.client.DefaultLoadBalancerRetryHandler;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import com.netflix.loadbalancer.reactive.ServerOperation;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/5/4.
 */
public class DrpcProxyFactory {
    public static final Logger LOGGER = LoggerFactory.getLogger(DrpcProxyFactory.class);

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private int maxActive = 32;
    private int maxWait = 1000;
    private int idleTime = 180000;

    private DrpcCluster cluster;
    private DrpcSessionFactory<DrpcSession> sessionFactory;

    private GenericKeyedObjectPool<Server, DrpcSession> sessionPool;
    private ILoadBalancer loadBalancer;
    private LoadBalancerCommand<Object> loadBalancerCommand;

    public void startup() throws Exception {
        cluster.startup();
        sessionPool = new GenericKeyedObjectPool(sessionFactory);
        sessionPool.setMaxActive(maxActive); // 能从池中借出的对象的最大数目
        sessionPool.setMaxIdle(20); // 池中可以空闲对象的最大数目
        sessionPool.setMinIdle(0);
        sessionPool.setMaxWait(maxWait); // 对象池空时调用borrowObject方法，最多等待多少毫秒
        sessionPool.setTestOnBorrow(true);
        sessionPool.setTestWhileIdle(true);
        sessionPool.setTimeBetweenEvictionRunsMillis(idleTime / 2);// 间隔每过多少毫秒进行一次后台对象清理的行动
        sessionPool.setNumTestsPerEvictionRun(-1);// －1表示清理时检查所有线程
        sessionPool.setMinEvictableIdleTimeMillis(idleTime);// 设定在进行后台对象清理时，休眠时间超过了3000毫秒的对象为过期
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
        private DrpcClientFactory clientFactory;
        private Method method;
        private Object[] args;

        public DrpcServerOperation(DrpcClientFactory clientFactory, Method method, Object[] args) {
            this.clientFactory = clientFactory;
            this.method = method;
            this.args = args;
        }

        @Override
        public Observable<Object> call(Server server) {
            DrpcSession session = null;
            try {
                session = sessionPool.borrowObject(server);
                Object client = clientFactory.makeClient(session);
                Object result = method.invoke(client, args);
                return Observable.just(result);
            } catch (InvocationTargetException e) {
                return Observable.error(e.getTargetException());
            } catch (Throwable e){
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

    public Object proxyClient(Class serviceClass, final DrpcClientFactory clientFactory) throws Exception {
        return Proxy.newProxyInstance(this.classLoader, new Class[]{serviceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                DrpcServerOperation operation = new DrpcServerOperation(clientFactory, method, args);
                Object result = loadBalancerCommand.submit(operation).toBlocking().first();
//                if (!args[0].equals(result))
//                    LOGGER.info(args[0] + "<>" + result);
                return result;
            }
        });
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
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

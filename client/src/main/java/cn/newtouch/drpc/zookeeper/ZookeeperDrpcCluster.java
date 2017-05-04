package cn.newtouch.drpc.zookeeper;

import cn.newtouch.drpc.DrpcCluster;
import com.netflix.loadbalancer.Server;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/3.
 */
public class ZookeeperDrpcCluster extends DrpcCluster {
    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDrpcCluster.class);

    private String namespace;
    private String connectString;

    private CuratorFramework zookeeper;
    private PathChildrenCache pathChildrenCache;

    public Set<Server> getServers(ChildData data) throws UnsupportedEncodingException {
        String addresses = new String(data.getData(), "UTF-8");
        Set<Server> servers = new HashSet<>();
        for (String address : addresses.split(";")) {
            String[] hostAndPort = address.split(":");
            String host = hostAndPort[0];
            int port =Integer.parseInt(hostAndPort[1]);
            servers.add(new Server(host, port));
        }
        return servers;
    }

    public void startup() throws Exception{
        zookeeper = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .canBeReadOnly(true)
                .namespace("drpc")
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .defaultData(null)
                .build();
        zookeeper.start();
        pathChildrenCache = new PathChildrenCache(zookeeper, namespace, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework zookeeper, PathChildrenCacheEvent event) throws Exception {
                PathChildrenCacheEvent.Type eventType = event.getType();
                LOGGER.info(eventType.name(), event.getData());
                switch (eventType) {
                    case CHILD_ADDED:
                        addServers(getServers(event.getData()));
                        break;
                    case CHILD_REMOVED:
                        removeServer(getServers(event.getData()));
                        break;
                }
            }
        });
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

    @Override
    public void shutdown() throws Exception {
        pathChildrenCache.close();
        pathChildrenCache = null;
        zookeeper.close();
        zookeeper = null;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }
}

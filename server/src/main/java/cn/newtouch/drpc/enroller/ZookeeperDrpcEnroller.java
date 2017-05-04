package cn.newtouch.drpc.enroller;

import cn.newtouch.drpc.DrpcEnroller;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/5/3.
 */
public class ZookeeperDrpcEnroller extends DrpcEnroller {
    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDrpcEnroller.class);

    private String connectString;

    private CuratorFramework zookeeper;

    @Override
    public synchronized void startup() throws Exception {
        if (zookeeper != null) return;
        LOGGER.info("zookeeper starting!");
        zookeeper = CuratorFrameworkFactory.builder()
                .connectString(this.connectString)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .canBeReadOnly(true)
                .namespace("drpc")
                .retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .defaultData(null).build();

        zookeeper.start();
        zookeeper.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if ((connectionState == ConnectionState.CONNECTED) || (connectionState == ConnectionState.RECONNECTED)) {
                    while (zookeeper != null) {
                        try {
                            if (curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                                ((ACLBackgroundPathAndBytesable) zookeeper.create()
                                        .creatingParentsIfNeeded()
                                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL))
                                        .forPath(getNamespace() + "/NODE_", getAddress().getBytes("UTF-8"));
                                LOGGER.info("register address:" + getAddress());
                                break;
                            }
                        } catch (InterruptedException e) {
                            LOGGER.error("register address failed", e);
                            break;
                        } catch (Exception e) {
                            LOGGER.debug("register address error", e);
                        }
                    }
                }
            }
        });
        LOGGER.info("zookeeper started!");
    }

    @Override
    public synchronized void shutdown() throws Exception {
        if (zookeeper != null) {
            LOGGER.info("zookeeper closing!");
            zookeeper.close();
            zookeeper = null;
            LOGGER.info("zookeeper closed!");
        }
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }
}

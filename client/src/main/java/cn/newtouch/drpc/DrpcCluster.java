package cn.newtouch.drpc;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/4.
 */
public abstract class DrpcCluster implements ServerList<Server> {

    private Set<Server> servers = new HashSet<>();

    public abstract void startup() throws Exception;

    public abstract void shutdown() throws Exception;

    protected synchronized void addServers(Set<Server> servers) {
        this.servers.addAll(servers);
    }

    protected synchronized void removeServer(Set<Server> servers) {
        this.servers.removeAll(servers);
    }
    @Override
    public synchronized List<Server> getInitialListOfServers() {
        return new ArrayList<>(servers);
    }

    @Override
    public synchronized List<Server> getUpdatedListOfServers() {
        return new ArrayList<>(servers);
    }
}

package cn.newtouch.drpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Created by Administrator on 2017/5/3.
 */
public class DrpcEmitter extends TimerTask {
    public static final Logger LOGGER = LoggerFactory.getLogger(DrpcEmitter.class);

    private boolean startup;
    private DrpcServer server;
    private DrpcEnroller enroller;
    private Timer timer = new Timer();

    @Override
    public void run() {
        try {
            if (startup && !server.isServing()) server.startup();
        } catch (Exception e) {
            LOGGER.debug("", e);
        }
    }

    private String address() throws SocketException {
        Set<String> ips = new HashSet();
        Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = netInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if ((!address.isAnyLocalAddress()) && (!address.isLoopbackAddress())
                        && (!address.isMulticastAddress()) && (address.getAddress().length == 4)) {
                    ips.add(address.getHostAddress());
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String ip : ips) {
            builder.append(ip).append(':').append(server.getPort()).append(';');
        }
        return builder.toString();
    }

    public synchronized void startup() throws Exception {
        if (!startup) {
            server.startup();
            enroller.setAddress(address());
            enroller.startup();
            startup = true;
            timer.schedule(this, 5000, 5000);
        }
    }

    public synchronized void shutdown() throws Exception {
        if (startup) {
            enroller.shutdown();
            server.shutdown();
            startup = false;
            timer.cancel();
        }
    }

    public DrpcServer getServer() {
        return server;
    }

    public void setServer(DrpcServer server) {
        this.server = server;
    }

    public DrpcEnroller getEnroller() {
        return enroller;
    }

    public void setEnroller(DrpcEnroller enroller) {
        this.enroller = enroller;
    }
}

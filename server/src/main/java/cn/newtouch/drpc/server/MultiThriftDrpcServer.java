package cn.newtouch.drpc.server;

import cn.newtouch.drpc.Drpc;
import cn.newtouch.drpc.DrpcServer;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/3.
 */
public class MultiThriftDrpcServer extends DrpcServer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MultiThriftDrpcServer.class);

    private int timeout = 60;
    private Map<String, Drpc> drpcs;

    private TServer server;

    @Override
    public boolean isServing() {
        return server != null && server.isServing();
    }

    @Override
    public synchronized void startup() throws Exception {
        if (server == null) {
            LOGGER.info("thrift creating");
            TMultiplexedProcessor processor = new TMultiplexedProcessor();
            for (Map.Entry<String, Drpc> entry : getDrpcs().entrySet()) {
                LOGGER.info("service name:" + entry.getKey());
                processor.registerProcessor(entry.getKey(), entry.getValue().getProcessor());
            }
            TServerTransport transport = new TServerSocket(getPort());
            TThreadPoolServer.Args args = new TThreadPoolServer.Args(transport);
            args.processor(processor);
            args.requestTimeout(this.timeout);
            args.protocolFactory(new TBinaryProtocol.Factory());

            server = new TThreadPoolServer(args);
            LOGGER.info("thrift created");
        }
        if (!isServing()) {
            LOGGER.info("thrift starting");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    server.serve();
                }
            });
            thread.setDaemon(true);
            thread.start();
            while (!isServing()) {
                thread.join(100);
            }
            LOGGER.info("thrift started");
        }
    }

    @Override
    public synchronized void shutdown() throws Exception {
        if (server != null) {
            LOGGER.info("thrift stopping");
            server.stop();
            server = null;
            LOGGER.info("thrift stopped");
        }
    }

    public Map<String, Drpc> getDrpcs() {
        return drpcs;
    }

    public void setDrpcs(Map<String, Drpc> drpcs) {
        this.drpcs = drpcs;
    }
}

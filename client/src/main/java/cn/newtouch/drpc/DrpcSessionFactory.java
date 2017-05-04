package cn.newtouch.drpc;

import com.netflix.loadbalancer.Server;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

/**
 * Created by Administrator on 2017/5/4.
 */
public abstract class DrpcSessionFactory<T extends DrpcSession> extends BaseKeyedPoolableObjectFactory<Server, T> {

}

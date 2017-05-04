package cn.newtouch.drpc.server;

import cn.newtouch.drpc.Drpc;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Created by Administrator on 2017/5/3.
 */
public class SpringMultiThriftDrpcServer extends MultiThriftDrpcServer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Map<String, Drpc> getDrpcs() {
        return applicationContext.getBeansOfType(Drpc.class);
    }
}

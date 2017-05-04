package cn.newtouch.drpc;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Administrator on 2017/5/4.
 */
public class SpringDrpcFactory extends DrpcFactory implements FactoryBean, InitializingBean, DisposableBean {
    @Override
    public Object getObject() throws Exception {
        return proxyClient();
    }

    @Override
    public Class getObjectType() {
        return getServiceClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startup();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }
}

package cn.newtouch.drpc;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Administrator on 2017/5/4.
 */
public class SpringDrpcProxyFactory extends DrpcProxyFactory implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        startup();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }
}

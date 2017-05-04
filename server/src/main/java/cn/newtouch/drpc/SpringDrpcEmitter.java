package cn.newtouch.drpc;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Administrator on 2017/5/3.
 */
public class SpringDrpcEmitter extends DrpcEmitter implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        startup();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }

}

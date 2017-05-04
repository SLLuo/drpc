package cn.newtouch.drpc.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TestServiceServer {

    public static void main(String[] args) throws InterruptedException, IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"thrift-server.xml"});
        context.start();
        System.in.read();
        context.destroy();
    }

}

package cn.newtouch.drpc.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestServiceClient {

    public static void main(String[] args) throws Exception {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"thrift-client.xml"});
        context.start();
        final TestService.Iface testService = (TestService.Iface) context.getBean("testService");
//        new TestServiceTest().test(testService);
        for (int i = 0; i < 100; i+=50) {
            for (int j = 0; j <= i; j++) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            new TestServiceTest().test(testService);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            System.out.println(">>>>" + i);
            Thread.sleep(3000);
        }
        System.in.read();
        context.destroy();
    }

}

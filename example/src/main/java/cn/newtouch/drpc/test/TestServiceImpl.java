package cn.newtouch.drpc.test;

import cn.newtouch.drpc.Drpc;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service("testService")
public class TestServiceImpl implements TestService.Iface, Drpc {

    public TProcessor getProcessor() {
        return new TestService.Processor<TestService.Iface>(this);
    }

    @Override
    public String test(String str) throws TException {
        try {
            Thread.sleep(Math.abs(new Random().nextLong() % 1000L));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public String test1() {
        return "test1";
    }

    @Override
    public String test2() {
        return "test2";
    }

    public List<Test> tests(List<Test> tests) throws TestException, TException {
        System.out.println(tests);
//        throw new TestException("ssss");
        return tests;
    }
}

package cn.newtouch.drpc.test;

import cn.newtouch.drpc.Drpc;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("testService")
public class TestServiceImpl implements TestService.Iface, Drpc {

    public TProcessor getProcessor() {
        return new TestService.Processor<TestService.Iface>(this);
    }

    @Override
    public Test test(Test test) throws TException {
        System.out.println(test);
        return test;
    }

    public List<Test> tests(List<Test> tests) throws TestException, TException {
        System.out.println(tests);
//        throw new TestException("ssss");
        return tests;
    }
}

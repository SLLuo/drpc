package cn.newtouch.drpc.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by Administrator on 2016/10/14.
 */
public class TestServiceTest {

    private static int i = 0;

    public static void testX(TestService.Iface testService) throws Exception {
        String str = "test" + i++;
        String _str = testService.test(str);
        if (!str.equals(_str)) {
            System.out.println(str + "!=" + _str);
        }
    }

    public void test(TestService.Iface testService) throws Exception {
        long time = System.currentTimeMillis();
        List<Test> tests = new ArrayList<Test>();
        for (int i = 0; i < 100; i++) {
            Test test = new Test();
            test.setValue1("value1" + i);
            test.setValue2("value2" + i);
            tests.add(test);
        }
        tests = testService.tests(tests);
        //System.out.println(tests.size());
    }
}

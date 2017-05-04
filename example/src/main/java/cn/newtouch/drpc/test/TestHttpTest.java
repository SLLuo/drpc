package cn.newtouch.drpc.test;

public class TestHttpTest {

    public static void main(String[] args) throws Exception {
        String[] params = new String[1];
        params[0] = "{\"value1\":\"value1\",\"value2\":\"value2\"}";
        String result = HttpThrift.exec("http://localhost:8080/test", "test", params);
        System.out.println(result);
    }
}

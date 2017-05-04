package cn.newtouch.drpc.test;

/**
 * Created by observer on 2017/3/25.
 */
public class UserHttpTest {

    public static void main(String[] args) throws Exception {
        String[] params = new String[1];
        params[0] = "{\"account\":\"account1\",\"password\":\"password1\",\"roles\":[{\"name\":\"name2\",\"perms\":[{\"name\":\"name1\",\"paths\":{\"name1\":{\"name\":\"name1\",\"value\":\"value1\"},\"name2\":{\"name\":\"name2\",\"value\":\"value2\"}}},{\"name\":\"name2\",\"paths\":{\"name1\":{\"name\":\"name1\",\"value\":\"value1\"},\"name2\":{\"name\":\"name2\",\"value\":\"value2\"}}}]},{\"name\":\"name1\",\"perms\":[{\"name\":\"name1\",\"paths\":{\"name1\":{\"name\":\"name1\",\"value\":\"value1\"},\"name2\":{\"name\":\"name2\",\"value\":\"value2\"}}},{\"name\":\"name2\",\"paths\":{\"name1\":{\"name\":\"name1\",\"value\":\"value1\"},\"name2\":{\"name\":\"name2\",\"value\":\"value2\"}}}]}]}";
        String result = HttpThrift.exec("http://localhost:8080/user", "user", params);
        System.out.println(result);
    }


}

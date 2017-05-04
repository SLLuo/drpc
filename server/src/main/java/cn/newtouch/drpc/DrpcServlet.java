package cn.newtouch.drpc;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by observer on 2017/3/25.
 */
public class DrpcServlet extends HttpServlet {

    private Drpc drpc;
    private Map<String, Method> methodMap;

    public DrpcServlet(Drpc drpc) {
        this.drpc = drpc;
        this.methodMap = new HashMap<>();
        Method[] methods = drpc.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!"getProcessor".equals(method.getName())) {
                method.setAccessible(true);
                methodMap.put(method.getName(), method);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mName = request.getHeader("method");
        List<String> lines = IOUtils.readLines(request.getInputStream(), "UTF-8");
        response.setContentType("text/json; charset=UTF-8");
        OutputStream os = response.getOutputStream();
        try {
            Method method = methodMap.get(mName);
            Class[] types = method.getParameterTypes();
            Object[] args = new Object[types.length];
            if (lines.size() < types.length) {
                os.write("request line size less then method params size!".getBytes("UTF-8"));
            }
            for (int i = 0; i < types.length; i++) {
                args[i] = JSON.parseObject(lines.get(i), types[i]);
            }
            Object result = method.invoke(drpc, args);
            if (result instanceof TBase) {
                ((TBase) result).write(new TSimpleJSONProtocol(new TIOStreamTransport(os)));
            } else {
                os.write(JSON.toJSONString(result).getBytes("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            os.write(e.toString().getBytes("UTF-8"));
        } finally {
            os.flush();
        }
    }

}

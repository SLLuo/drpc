package cn.newtouch.drpc.test;

import cn.newtouch.drpc.Drpc;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.springframework.stereotype.Service;

/**
 * Created by observer on 2017/3/26.
 */
@Service("userService")
public class UserServiceImpl implements UserService.Iface, Drpc {
    @Override
    public TProcessor getProcessor() {
        return new UserService.Processor<UserService.Iface>(this);
    }

    @Override
    public User user(User user) throws TException {
        System.out.println(user);
        return user;
    }
}

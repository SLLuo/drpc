namespace java cn.newtouch.drpc.test

struct Test {
	1:required string value1;
	2:required string value2;
}

exception TestException {
	1:required string message
}

service TestService {
    string test(1:string str);
    string test1();
    string test2();
    list<Test> tests(1:required list<Test> tests) throws (1:TestException te);
}
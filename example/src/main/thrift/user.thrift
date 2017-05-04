namespace java cn.newtouch.drpc.test

struct Path {
	1:required string name;
	2:required string value;
}

struct Perm {
	1:required string name;
	2:required map<string, Path> paths;
}

struct Role {
	1:required string name;
	2:required list<Perm> perms;
}

struct User {
	1:required string account;
	2:required string password;
	3:required set<Role> roles;
}

service UserService {
    User user(1:required User user);
}
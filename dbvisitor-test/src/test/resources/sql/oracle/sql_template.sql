DROP TABLE test_md5_user PURGE;
CREATE TABLE test_md5_user (
    id varchar2(50) primary key,
    name varchar2(100),
    password varchar2(100)
);

DROP TABLE test_template_user PURGE;
CREATE TABLE test_template_user (
    id number(10) primary key,
    name varchar2(50),
    login_ip varchar2(50),
    create_at timestamp,
    data varchar2(200)
);

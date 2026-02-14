DROP TABLE IF EXISTS test_md5_user;
CREATE TABLE test_md5_user (
    id varchar(50) primary key,
    name varchar(100),
    password varchar(100)
);

DROP TABLE IF EXISTS test_template_user;
CREATE TABLE test_template_user (
    id int primary key,
    name varchar(50),
    login_ip varchar(50),
    create_at timestamp,
    data varchar(200)
);

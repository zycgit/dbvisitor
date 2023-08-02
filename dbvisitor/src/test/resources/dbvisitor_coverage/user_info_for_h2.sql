create table user_info
(
    user_uuid      varchar(50) not null primary key,
    user_name      varchar(100) null,
    login_name     varchar(100) null,
    login_password varchar(100) null,
    email          varchar(200) null,
    seq            int null,
    register_time  timestamp null
)

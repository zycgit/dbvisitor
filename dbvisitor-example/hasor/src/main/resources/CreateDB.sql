drop table if exists `stb_user`;
create table `stb_user`
(
    id          bigint auto_increment,
    name        varchar(100),
    gender      varchar(10),
    email       varchar(100),
    role_id     bigint,
    create_time datetime default now(),
    modify_time datetime on update now(),
    primary key (`id`)
);

insert into `stb_user` (id, name, gender, email, role_id, create_time, modify_time)
values (1, 'mali', 'F', 'mali@hasor.net', 1, now(), now()),
       (2, 'dative', 'T', 'dative@hasor.net', 1, now(), now()),
       (3, 'jon wes', 'F', 'jon@hasor.net', 2, now(), now()),
       (4, 'mary', 'T', 'mary@hasor.net', 3, now(), now()),
       (5, 'matt', 'T', 'matt@hasor.net', 3, now(), now());

drop table if exists `stb_role`;
create table `stb_role`
(
    id          bigint auto_increment,
    name        varchar(100),
    create_time datetime default now(),
    modify_time datetime on update now(),
    primary key (`id`)
);

insert into `stb_role` (id, name, create_time, modify_time)
values (1, 'Admin', now(), now()),
       (2, 'User', now(), now()),
       (3, 'Guest', now(), now());

drop table if exists `test_user`;
create table `test_user`
(
    `id`          int(11) auto_increment,
    `name`        varchar(255),
    `age`         int,
    `create_time` datetime,
    primary key (`id`)
);

insert into `test_user`
values (1, 'mali', 26, now());
insert into `test_user`
values (2, 'dative', 32, now());
insert into `test_user`
values (3, 'jon wes', 41, now());
insert into `test_user`
values (4, 'mary', 66, now());
insert into `test_user`
values (5, 'matt', 25, now());


drop procedure if exists proc_select_table;
create procedure proc_select_table(in userName varchar (200), out outName varchar (200))
begin
select *
from test_user
where name = userName;
select *
from test_user;
set
outName = concat(userName,'-str') ;
end;


drop procedure if exists proc_select_table_result;
create procedure proc_select_table_result(in userName varchar (200))
begin
select *
from test_user
where name = userName;
end;
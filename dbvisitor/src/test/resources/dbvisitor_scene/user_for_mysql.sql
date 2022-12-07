create table user(
    id          int(11) auto_increment primary key,
    name        varchar(255),
    age         int,
    create_time datetime
);

create procedure proc_select_gt_users(in p_age varchar(200))
begin
    select * from user where age > p_age;
end;

create procedure proc_select_gt_users_repeat(in p_age varchar(200))
begin
    select * from user where age > p_age;
    select * from user where age > p_age;
end;
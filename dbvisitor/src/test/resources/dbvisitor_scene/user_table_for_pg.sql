create table user_table(
    id          serial primary key,
    name        varchar(255),
    age         int,
    create_time timestamp without time zone
);

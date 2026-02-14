DROP TABLE IF EXISTS test_special_types;
CREATE TABLE test_special_types (
    id int primary key,
    json_map varchar(1000),
    json_list varchar(1000),
    json_set varchar(1000),
    int_array integer[]
);

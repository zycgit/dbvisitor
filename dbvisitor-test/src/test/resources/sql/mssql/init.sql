DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS [Case_Test_Upper];
DROP TABLE IF EXISTS case_test_lower;
DROP TABLE IF EXISTS array_types_annotation_test;
DROP TABLE IF EXISTS binary_types_explicit_test;
DROP TABLE IF EXISTS enum_types_explicit_test;
DROP TABLE IF EXISTS time_types_explicit_test;
DROP TABLE IF EXISTS json_types_explicit_test;
DROP TABLE IF EXISTS basic_types_explicit_test;
DROP TABLE IF EXISTS basic_types_test;
DROP TABLE IF EXISTS array_types_explicit_test;
DROP TABLE IF EXISTS array_types_test;
DROP TABLE IF EXISTS test_special_types;
DROP TABLE IF EXISTS complex_order;
DROP TABLE IF EXISTS product_vector;
DROP TABLE IF EXISTS user_order;
DROP TABLE IF EXISTS user_info;
DROP SEQUENCE IF EXISTS user_order_id_seq;
DROP SEQUENCE IF EXISTS user_info_id_seq;

CREATE TABLE user_role (
    user_id     INT NOT NULL,
    role_id     INT NOT NULL,
    role_name   VARCHAR(100),
    create_time DATETIME2(3),
    PRIMARY KEY (user_id, role_id)
);

CREATE SEQUENCE user_info_id_seq START WITH 100000 INCREMENT BY 1;

CREATE TABLE user_info (
    id INT PRIMARY KEY DEFAULT NEXT VALUE FOR user_info_id_seq,
    name NVARCHAR(100),
    age INT,
    email NVARCHAR(100),
    create_time DATETIME2(3) DEFAULT SYSDATETIME()
);

CREATE SEQUENCE user_order_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE user_order (
    id INT PRIMARY KEY DEFAULT NEXT VALUE FOR user_order_id_seq,
    user_id INT,
    order_no NVARCHAR(100),
    amount DECIMAL(10, 2),
    create_time DATETIME2(3) DEFAULT SYSDATETIME()
);

CREATE INDEX idx_user_order_user_id ON user_order(user_id);

CREATE TABLE product_vector (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    embedding NVARCHAR(MAX)
);

CREATE TABLE complex_order (
    id INT PRIMARY KEY,
    order_no VARCHAR(100),
    address NVARCHAR(MAX),
    items NVARCHAR(MAX)
);

CREATE TABLE basic_types_test (
    id           INT PRIMARY KEY,
    byte_value   TINYINT,
    short_value  SMALLINT,
    int_value    INT,
    long_value   BIGINT,
    float_value  REAL,
    double_value FLOAT,
    decimal_value DECIMAL(10, 2),
    big_int_value DECIMAL(20, 0),
    bool_value   BIT,
    string_value VARCHAR(255),
    char_value   CHAR(1)
);

CREATE TABLE basic_types_explicit_test (
    id             INT PRIMARY KEY,
    byte_value     TINYINT,
    short_value    SMALLINT,
    int_value      INT,
    long_value     BIGINT,
    float_value    REAL,
    double_value   FLOAT,
    decimal_value  DECIMAL(10, 2),
    big_int_value  DECIMAL(20, 0),
    bool_bit       BIT,
    bool_boolean   BIT,
    char_value     CHAR(1),
    varchar_value  VARCHAR(255),
    nvarchar_value NVARCHAR(255)
);

CREATE TABLE array_types_test (
    id            INT PRIMARY KEY,
    int_array     NVARCHAR(MAX),
    string_array  NVARCHAR(MAX),
    float_array   NVARCHAR(MAX)
);

CREATE TABLE array_types_explicit_test (
    id            INT PRIMARY KEY,
    int_array     NVARCHAR(MAX),
    varchar_array NVARCHAR(MAX)
);

CREATE TABLE array_types_annotation_test (
    id                      INT PRIMARY KEY,
    array_no_annotation     NVARCHAR(MAX),
    array_jdbc_type         NVARCHAR(MAX),
    array_type_handler      NVARCHAR(MAX),
    array_number_special    NVARCHAR(MAX),
    array_full_annotated    NVARCHAR(MAX)
);

CREATE TABLE test_special_types (
    id        INT PRIMARY KEY,
    json_map  NVARCHAR(MAX),
    json_list NVARCHAR(MAX),
    json_set  NVARCHAR(MAX),
    int_array NVARCHAR(MAX)
);

CREATE TABLE binary_types_explicit_test (
    id                    INT PRIMARY KEY,
    binary_value          VARBINARY(1000),
    varbinary_value       VARBINARY(MAX),
    longvarbinary_value   VARBINARY(MAX),
    blob_value            VARBINARY(MAX)
);

CREATE TABLE enum_types_explicit_test (
    id               INT PRIMARY KEY,
    status_string    VARCHAR(50),
    status_enum_code VARCHAR(50),
    status_ordinal   INT,
    status_code      INT
);

CREATE TABLE time_types_explicit_test (
    id                  INT PRIMARY KEY,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     DATETIME2(3),
    local_date_ts       DATETIME2(3),
    local_time_ts       DATETIME2(3),
    local_datetime_ts   DATETIME2(3),
    julian_day          BIGINT
);

CREATE TABLE json_types_explicit_test (
    id           INT PRIMARY KEY,
    json_varchar NVARCHAR(2000),
    json_mysql   NVARCHAR(MAX),
    nested_json  NVARCHAR(MAX)
);

CREATE TABLE case_test_lower (
    id          INT PRIMARY KEY,
    name        VARCHAR(100),
    age         INT,
    memo        VARCHAR(200)
);

CREATE TABLE [Case_Test_Upper] (
    [Id]        INT PRIMARY KEY,
    [Name]      VARCHAR(100),
    [Age]       INT,
    [Memo]      VARCHAR(200)
);

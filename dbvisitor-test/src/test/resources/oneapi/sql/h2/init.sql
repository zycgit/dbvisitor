DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS binary_types_explicit_test;
DROP TABLE IF EXISTS enum_types_explicit_test;
DROP TABLE IF EXISTS json_types_explicit_test;
DROP TABLE IF EXISTS basic_types_explicit_test;
DROP TABLE IF EXISTS basic_types_test;
DROP TABLE IF EXISTS array_types_explicit_test;
DROP TABLE IF EXISTS array_types_test;
DROP TABLE IF EXISTS complex_order;
DROP TABLE IF EXISTS product_vector;
DROP TABLE IF EXISTS user_order;
DROP TABLE IF EXISTS user_info;

CREATE TABLE user_role (
    user_id     INT NOT NULL,
    role_id     INT NOT NULL,
    role_name   VARCHAR(100),
    create_time TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP
);

CREATE TABLE user_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time TIMESTAMP
);

CREATE TABLE product_vector (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    embedding REAL ARRAY
);

CREATE TABLE complex_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(100),
    address VARCHAR(2000),
    items VARCHAR(2000)
);

CREATE TABLE basic_types_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    byte_value   TINYINT,
    short_value  SMALLINT,
    int_value    INT,
    long_value   BIGINT,
    float_value  REAL,
    double_value DOUBLE,
    decimal_value DECIMAL(10, 2),
    big_int_value NUMERIC(20),
    bool_value   BOOLEAN,
    string_value VARCHAR(255),
    char_value   CHAR(1)
);

CREATE TABLE basic_types_explicit_test (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    byte_value     TINYINT,
    short_value    SMALLINT,
    int_value      INT,
    long_value     BIGINT,
    float_value    REAL,
    double_value   DOUBLE,
    decimal_value  DECIMAL(10, 2),
    big_int_value  NUMERIC(20),
    bool_bit       BIT,
    bool_boolean   BOOLEAN,
    char_value     CHAR(1),
    varchar_value  VARCHAR(255),
    nvarchar_value NVARCHAR(255)
);

CREATE TABLE time_types_test (
    id                INT PRIMARY KEY AUTO_INCREMENT,
    util_date         TIMESTAMP,
    sql_date          DATE,
    sql_time          TIME,
    sql_timestamp     TIMESTAMP,
    local_date        DATE,
    local_time        TIME,
    local_datetime    TIMESTAMP,
    offset_datetime   TIMESTAMP WITH TIME ZONE,
    zoned_datetime    TIMESTAMP WITH TIME ZONE,
    instant           TIMESTAMP
);

CREATE TABLE time_types_explicit_test (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     TIMESTAMP,
    local_date_ts       TIMESTAMP,
    local_time_ts       TIMESTAMP,
    local_datetime_ts   TIMESTAMP,
    julian_day          BIGINT
);

CREATE TABLE array_types_test (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    int_array     INT ARRAY,
    string_array  VARCHAR ARRAY,
    float_array   REAL ARRAY
);

CREATE TABLE array_types_explicit_test (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    int_array       INT ARRAY,
    varchar_array   VARCHAR ARRAY
);

CREATE TABLE array_types_annotation_test (
    id                      INT PRIMARY KEY AUTO_INCREMENT,
    array_no_annotation     INT ARRAY,
    array_jdbc_type         INT ARRAY,
    array_type_handler      INT ARRAY,
    array_number_special    INT ARRAY,
    array_full_annotated    INT ARRAY
);

-- Binary Types Test Tables
CREATE TABLE binary_types_explicit_test (
    id                      INT PRIMARY KEY AUTO_INCREMENT,
    binary_value            VARBINARY(1000),
    varbinary_value         VARBINARY(100000),
    longvarbinary_value     BLOB,
    blob_value              BLOB
);

-- Enum Types Test Tables  
CREATE TABLE enum_types_explicit_test (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    status_string   VARCHAR(50),
    status_enum_code VARCHAR(50),
    status_ordinal  INT,
    status_code     INT
);

-- JSON Types Test Tables
CREATE TABLE json_types_explicit_test (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    json_varchar    VARCHAR(2000),
    json_mysql      VARCHAR(2000),
    nested_json     VARCHAR(2000)
);


CREATE DATABASE IF NOT EXISTS devtester DEFAULT CHARSET=utf8mb4;
USE devtester;

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS array_types_annotation_test;
DROP TABLE IF EXISTS binary_types_explicit_test;
DROP TABLE IF EXISTS enum_types_explicit_test;
DROP TABLE IF EXISTS time_types_explicit_test;
DROP TABLE IF EXISTS json_types_explicit_test;
DROP TABLE IF EXISTS array_types_explicit_test;
DROP TABLE IF EXISTS array_types_test;
DROP TABLE IF EXISTS test_special_types;
DROP TABLE IF EXISTS basic_types_explicit_test;
DROP TABLE IF EXISTS basic_types_test;
DROP TABLE IF EXISTS complex_order;
DROP TABLE IF EXISTS product_vector;
DROP TABLE IF EXISTS user_order;
DROP TABLE IF EXISTS user_info;

CREATE TABLE user_role (
    user_id     INT NOT NULL,
    role_id     INT NOT NULL,
    role_name   VARCHAR(100),
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_vector (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    embedding JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE complex_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(100),
    address JSON,
    items JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE basic_types_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    byte_value   TINYINT,
    short_value  SMALLINT,
    int_value    INT,
    long_value   BIGINT,
    float_value  FLOAT,
    double_value DOUBLE,
    decimal_value DECIMAL(10, 2),
    big_int_value DECIMAL(20, 0),
    bool_value   BOOLEAN,
    string_value VARCHAR(255),
    char_value   CHAR(1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE basic_types_explicit_test (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    byte_value     TINYINT,
    short_value    SMALLINT,
    int_value      INT,
    long_value     BIGINT,
    float_value    FLOAT,
    double_value   DOUBLE,
    decimal_value  DECIMAL(10, 2),
    big_int_value  DECIMAL(20, 0),
    bool_bit       BIT(1),
    bool_boolean   BOOLEAN,
    char_value     CHAR(1),
    varchar_value  VARCHAR(255),
    nvarchar_value VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE array_types_test (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    int_array     JSON,
    string_array  JSON,
    float_array   JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE array_types_explicit_test (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    int_array     JSON,
    varchar_array JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE array_types_annotation_test (
    id                      INT PRIMARY KEY AUTO_INCREMENT,
    array_no_annotation     JSON,
    array_jdbc_type         JSON,
    array_type_handler      JSON,
    array_number_special    JSON,
    array_full_annotated    JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE test_special_types (
    id        INT PRIMARY KEY,
    json_map  JSON,
    json_list JSON,
    json_set  JSON,
    int_array JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE binary_types_explicit_test (
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    binary_value          VARBINARY(1000),
    varbinary_value       LONGBLOB,
    longvarbinary_value   LONGBLOB,
    blob_value            LONGBLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enum_types_explicit_test (
    id               INT PRIMARY KEY AUTO_INCREMENT,
    status_string    VARCHAR(50),
    status_enum_code VARCHAR(50),
    status_ordinal   INT,
    status_code      INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE time_types_explicit_test (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     DATETIME(3) NULL,
    local_date_ts       DATETIME(3) NULL,
    local_time_ts       DATETIME(3) NULL,
    local_datetime_ts   DATETIME(3) NULL,
    julian_day          BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE json_types_explicit_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    json_varchar VARCHAR(2000),
    json_mysql   JSON,
    nested_json  JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

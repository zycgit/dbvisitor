CREATE DATABASE IF NOT EXISTS devtester DEFAULT CHARSET=utf8mb4;
USE devtester;

DROP TABLE IF EXISTS user_role;
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
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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

-- MySQL 数据库 - 基本类型测试表

-- 用户视角：自动类型推断
CREATE TABLE IF NOT EXISTS basic_types_test (
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

-- 数据库类型视角：显式类型控制
CREATE TABLE IF NOT EXISTS basic_types_explicit_test (
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

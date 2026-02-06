DROP TABLE IF EXISTS binary_types_explicit_test CASCADE;
DROP TABLE IF EXISTS enum_types_explicit_test CASCADE;
DROP TABLE IF EXISTS time_types_explicit_test CASCADE;
DROP TABLE IF EXISTS json_types_explicit_test CASCADE;
DROP TABLE IF EXISTS basic_types_explicit_test CASCADE;
DROP TABLE IF EXISTS basic_types_test CASCADE;
DROP TABLE IF EXISTS array_types_explicit_test CASCADE;
DROP TABLE IF EXISTS array_types_test CASCADE;
DROP TABLE IF EXISTS array_types_annotation_test CASCADE;
DROP TABLE IF EXISTS complex_order CASCADE;
DROP TABLE IF EXISTS product_vector CASCADE;
DROP TABLE IF EXISTS user_order CASCADE;
DROP TABLE IF EXISTS user_info CASCADE;

CREATE TABLE user_info (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_order (
    id SERIAL PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_order_user_id ON user_order(user_id);

CREATE TABLE product_vector (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    embedding REAL[]
);

CREATE TABLE complex_order (
    id SERIAL PRIMARY KEY,
    order_no VARCHAR(100),
    address JSONB,
    items JSONB
);

CREATE TABLE basic_types_test (
    id           SERIAL PRIMARY KEY,
    byte_value   SMALLINT,
    short_value  SMALLINT,
    int_value    INTEGER,
    long_value   BIGINT,
    float_value  REAL,
    double_value DOUBLE PRECISION,
    decimal_value DECIMAL(10, 2),
    big_int_value NUMERIC(20),
    bool_value   BOOLEAN,
    string_value VARCHAR(255),
    char_value   CHAR(1)
);

CREATE TABLE basic_types_explicit_test (
    id             SERIAL PRIMARY KEY,
    byte_value     SMALLINT,
    short_value    SMALLINT,
    int_value      INTEGER,
    long_value     BIGINT,
    float_value    REAL,
    double_value   DOUBLE PRECISION,
    decimal_value  DECIMAL(10, 2),
    big_int_value  NUMERIC(20),
    bool_bit       BIT(1),
    bool_boolean   BOOLEAN,
    char_value     CHAR(1),
    varchar_value  VARCHAR(255),
    nvarchar_value VARCHAR(255)
);

-- Array Types Test Tables (PostgreSQL fully supports array types)
CREATE TABLE array_types_test (
    id            SERIAL PRIMARY KEY,
    int_array     INTEGER[],
    string_array  VARCHAR[],
    float_array   REAL[]
);

CREATE TABLE array_types_explicit_test (
    id            SERIAL PRIMARY KEY,
    int_array     INTEGER[],
    varchar_array VARCHAR[]
);

CREATE TABLE array_types_annotation_test (
    id                      SERIAL PRIMARY KEY,
    array_no_annotation     INTEGER[],
    array_jdbc_type         INTEGER[],
    array_type_handler      INTEGER[],
    array_number_special    INTEGER[],
    array_full_annotated    INTEGER[]
);

-- Binary Types Test Tables
CREATE TABLE binary_types_explicit_test (
    id                      SERIAL PRIMARY KEY,
    binary_value            BYTEA,
    varbinary_value         BYTEA,
    longvarbinary_value     BYTEA,
    blob_value              BYTEA
);

-- Enum Types Test Tables
CREATE TABLE enum_types_explicit_test (
    id              SERIAL PRIMARY KEY,
    status_string   VARCHAR(50),
    status_ordinal  INTEGER,
    status_code     INTEGER
);

-- Time Types Test Tables
CREATE TABLE time_types_explicit_test (
    id                  SERIAL PRIMARY KEY,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     TIMESTAMP,
    local_date_ts       TIMESTAMP,
    local_time_ts       TIMESTAMP,
    local_datetime_ts   TIMESTAMP
);

-- JSON Types Test Tables  
CREATE TABLE json_types_explicit_test (
    id              SERIAL PRIMARY KEY,
    json_varchar    VARCHAR(2000),
    json_mysql      VARCHAR(2000),
    nested_json     JSONB
);


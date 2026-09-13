DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS basic_types_explicit_test;
DROP TABLE IF EXISTS basic_types_test;
DROP TABLE IF EXISTS binary_types_explicit_test;
DROP TABLE IF EXISTS enum_types_explicit_test;
DROP TABLE IF EXISTS time_types_explicit_test;
DROP TABLE IF EXISTS json_types_explicit_test;
DROP TABLE IF EXISTS array_types_annotation_test;
DROP TABLE IF EXISTS array_types_explicit_test;
DROP TABLE IF EXISTS array_types_test;
DROP TABLE IF EXISTS test_special_types;
DROP TABLE IF EXISTS complex_order;
DROP TABLE IF EXISTS product_vector;
DROP TABLE IF EXISTS user_order;
DROP TABLE IF EXISTS user_info;

CREATE TABLE user_role (
    user_id     Int32,
    role_id     Int32,
    role_name   Nullable(String),
    create_time Nullable(DateTime64(3))
) ENGINE = MergeTree ORDER BY (user_id, role_id);

CREATE TABLE user_info (
    id Int32 DEFAULT cityHash64(generateUUIDv4()),
    name Nullable(String),
    age Nullable(Int32),
    email Nullable(String),
    create_time Nullable(DateTime64(3)) DEFAULT now64(3)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE user_order (
    id Int32 DEFAULT cityHash64(generateUUIDv4()),
    user_id Nullable(Int32),
    order_no Nullable(String),
    amount Nullable(Decimal(10, 2)),
    create_time DateTime64(3) DEFAULT now64(3)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE product_vector (
    id Int32,
    name Nullable(String),
    embedding Array(Float32)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE complex_order (
    id Int32,
    order_no Nullable(String),
    address Nullable(String),
    items Nullable(String)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE basic_types_test (
    id           Int32,
    byte_value   Nullable(Int16),
    short_value  Nullable(Int16),
    int_value    Nullable(Int32),
    long_value   Nullable(Int64),
    float_value  Nullable(Float32),
    double_value Nullable(Float64),
    decimal_value Nullable(Decimal(10, 2)),
    big_int_value Nullable(Decimal(20, 0)),
    bool_value   Nullable(Bool),
    string_value Nullable(String),
    char_value   Nullable(String)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE basic_types_explicit_test (
    id             Int32,
    byte_value     Nullable(Int16),
    short_value    Nullable(Int16),
    int_value      Nullable(Int32),
    long_value     Nullable(Int64),
    float_value    Nullable(Float32),
    double_value   Nullable(Float64),
    decimal_value  Nullable(Decimal(10, 2)),
    big_int_value  Nullable(Decimal(20, 0)),
    bool_bit       Nullable(Bool),
    bool_boolean   Nullable(Bool),
    char_value     Nullable(String),
    varchar_value  Nullable(String),
    nvarchar_value Nullable(String)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE array_types_test (
    id            Int32,
    int_array     Array(Nullable(Int32)),
    string_array  Array(Nullable(String)),
    float_array   Array(Nullable(Float32))
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE array_types_explicit_test (
    id            Int32,
    int_array     Array(Nullable(Int32)),
    varchar_array Array(Nullable(String))
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE array_types_annotation_test (
    id                      Int32,
    array_no_annotation     Array(Nullable(Int32)),
    array_jdbc_type         Array(Nullable(Int32)),
    array_type_handler      Array(Nullable(Int32)),
    array_number_special    Array(Nullable(Int32)),
    array_full_annotated    Array(Nullable(Int32))
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE test_special_types (
    id        Int32,
    json_map  Nullable(String),
    json_list Nullable(String),
    json_set  Nullable(String),
    int_array Array(Nullable(Int32))
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE binary_types_explicit_test (
    id                    Int32,
    binary_value          Nullable(String),
    varbinary_value       Nullable(String),
    longvarbinary_value   Nullable(String),
    blob_value            Nullable(String)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE enum_types_explicit_test (
    id               Int32,
    status_string    Nullable(String),
    status_enum_code Nullable(String),
    status_ordinal   Nullable(Int32),
    status_code      Nullable(Int32)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE time_types_explicit_test (
    id                  Int32,
    date_value          Nullable(Date32),
    time_value          Nullable(String),
    timestamp_value     Nullable(DateTime64(3)),
    local_date_ts       Nullable(DateTime64(3)),
    local_time_ts       Nullable(DateTime64(3)),
    local_datetime_ts   Nullable(DateTime64(3)),
    julian_day          Nullable(Int64)
) ENGINE = MergeTree ORDER BY id;

CREATE TABLE json_types_explicit_test (
    id           Int32,
    json_varchar Nullable(String),
    json_mysql   Nullable(String),
    nested_json  Nullable(String)
) ENGINE = MergeTree ORDER BY id;

-- Array Types Test Tables for H2

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

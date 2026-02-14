-- Array Types Test Tables for MySQL
-- Note: MySQL does not support native array types, storing as JSON or TEXT

CREATE TABLE array_types_test (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    int_array     JSON,
    string_array  JSON,
    float_array   JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE array_types_explicit_test (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    int_array       JSON,
    varchar_array   JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

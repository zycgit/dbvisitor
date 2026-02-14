-- Binary Types Test Tables for MySQL

CREATE TABLE binary_types_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    binary_data  VARBINARY(255),
    small_blob   BLOB,
    medium_blob  MEDIUMBLOB,
    large_blob   LONGBLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE binary_types_explicit_test (
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    binary_value          BINARY(16),
    varbinary_value       VARBINARY(255),
    longvarbinary_value   LONGBLOB,
    blob_value            BLOB
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

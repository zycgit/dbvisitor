-- Binary Types Test Tables for H2

CREATE TABLE binary_types_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    binary_data  VARBINARY(255),
    small_blob   BLOB,
    medium_blob  BLOB,
    large_blob   BLOB
);

CREATE TABLE binary_types_explicit_test (
    id                    INT PRIMARY KEY AUTO_INCREMENT,
    binary_value          BINARY(16),
    varbinary_value       VARBINARY(255),
    longvarbinary_value   BLOB,
    blob_value            BLOB
);

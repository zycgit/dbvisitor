-- Binary Types Test Tables for PostgreSQL

CREATE TABLE binary_types_test (
    id           SERIAL PRIMARY KEY,
    binary_data  BYTEA,
    small_blob   BYTEA,
    medium_blob  BYTEA,
    large_blob   BYTEA
);

CREATE TABLE binary_types_explicit_test (
    id                    SERIAL PRIMARY KEY,
    binary_value          BYTEA,
    varbinary_value       BYTEA,
    longvarbinary_value   BYTEA,
    blob_value            BYTEA
);

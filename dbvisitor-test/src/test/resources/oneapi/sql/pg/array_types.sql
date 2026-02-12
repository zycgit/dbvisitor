-- Array Types Test Tables for PostgreSQL

CREATE TABLE array_types_test (
    id            SERIAL PRIMARY KEY,
    int_array     INTEGER[],
    string_array  VARCHAR[],
    float_array   REAL[]
);

CREATE TABLE array_types_explicit_test (
    id              SERIAL PRIMARY KEY,
    int_array       INTEGER[],
    varchar_array   VARCHAR[]
);

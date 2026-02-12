-- Enum Types Test Tables for PostgreSQL

CREATE TABLE enum_types_test (
    id              SERIAL PRIMARY KEY,
    status          VARCHAR(50),
    status_string   VARCHAR(50),
    status_ordinal  INT
);

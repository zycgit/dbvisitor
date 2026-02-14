-- JSON Types Test Tables for PostgreSQL

CREATE TABLE json_types_test (
    id           SERIAL PRIMARY KEY,
    json_data    JSONB,
    json_array   JSONB,
    json_object  JSONB
);

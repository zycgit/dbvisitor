-- Time Types Test Tables for PostgreSQL

CREATE TABLE time_types_test (
    id                SERIAL PRIMARY KEY,
    util_date         TIMESTAMP,
    sql_date          DATE,
    sql_time          TIME,
    sql_timestamp     TIMESTAMP,
    local_date        DATE,
    local_time        TIME,
    local_datetime    TIMESTAMP,
    offset_datetime   TIMESTAMP WITH TIME ZONE,
    zoned_datetime    TIMESTAMP WITH TIME ZONE,
    instant           TIMESTAMP
);

CREATE TABLE time_types_explicit_test (
    id                  SERIAL PRIMARY KEY,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     TIMESTAMP,
    local_date_ts       TIMESTAMP,
    local_time_ts       TIMESTAMP,
    local_datetime_ts   TIMESTAMP
);

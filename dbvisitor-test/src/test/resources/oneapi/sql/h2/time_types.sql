-- Time Types Test Tables for H2

CREATE TABLE time_types_test (
    id                INT PRIMARY KEY AUTO_INCREMENT,
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
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     TIMESTAMP,
    local_date_ts       TIMESTAMP,
    local_time_ts       TIMESTAMP,
    local_datetime_ts   TIMESTAMP
);

-- Time Types Test Tables for MySQL

CREATE TABLE time_types_test (
    id                INT PRIMARY KEY AUTO_INCREMENT,
    util_date         DATETIME,
    sql_date          DATE,
    sql_time          TIME,
    sql_timestamp     TIMESTAMP,
    local_date        DATE,
    local_time        TIME,
    local_datetime    DATETIME,
    offset_datetime   DATETIME,
    zoned_datetime    DATETIME,
    instant           TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE time_types_explicit_test (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    date_value          DATE,
    time_value          TIME,
    timestamp_value     TIMESTAMP,
    local_date_ts       TIMESTAMP,
    local_time_ts       TIMESTAMP,
    local_datetime_ts   TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

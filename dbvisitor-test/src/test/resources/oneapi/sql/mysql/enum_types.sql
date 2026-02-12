-- Enum Types Test Tables for MySQL

CREATE TABLE enum_types_test (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    status          ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'DELETED'),
    status_string   VARCHAR(50),
    status_ordinal  INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

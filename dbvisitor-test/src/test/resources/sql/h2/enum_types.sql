-- Enum Types Test Tables for H2

CREATE TABLE enum_types_test (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    status          VARCHAR(50),
    status_string   VARCHAR(50),
    status_ordinal  INT
);

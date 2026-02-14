-- JSON Types Test Tables for MySQL

CREATE TABLE json_types_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    json_data    JSON,
    json_array   JSON,
    json_object  JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- JSON Types Test Tables for H2

CREATE TABLE json_types_test (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    json_data    VARCHAR(2000),
    json_array   VARCHAR(2000),
    json_object  VARCHAR(2000)
);

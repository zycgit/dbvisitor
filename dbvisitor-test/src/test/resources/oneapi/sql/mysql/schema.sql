-- MySQL Schema for One API Tests

-- UserInfo
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- UserOrder
DROP TABLE IF EXISTS user_order;
CREATE TABLE user_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ProductVector (MySQL 8.0+ doesn't have native vector, simulate with JSON/BLOB)
DROP TABLE IF EXISTS product_vector;
CREATE TABLE product_vector (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    embedding JSON  -- Store as JSON array in MySQL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ComplexOrder (Level 5 - JSON native support in MySQL 5.7+)
DROP TABLE IF EXISTS complex_order;
CREATE TABLE complex_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(100),
    address JSON,
    items JSON
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

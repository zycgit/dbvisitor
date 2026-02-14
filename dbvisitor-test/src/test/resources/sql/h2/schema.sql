-- UserInfo
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP
);

-- UserOrder
DROP TABLE IF EXISTS user_order;
CREATE TABLE user_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time TIMESTAMP
);

-- ProductVector (Simulated for H2)
DROP TABLE IF EXISTS product_vector;
CREATE TABLE product_vector (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    embedding REAL ARRAY -- standard array of floats
);

-- ComplexOrder (JSON storage simulation)
DROP TABLE IF EXISTS complex_order;
CREATE TABLE complex_order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(100),
    address VARCHAR(2000),
    items VARCHAR(2000)
);
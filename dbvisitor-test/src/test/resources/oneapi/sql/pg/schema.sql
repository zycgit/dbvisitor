-- PostgreSQL Schema for One API Tests

-- UserInfo
DROP TABLE IF EXISTS user_info CASCADE;
CREATE TABLE user_info (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- UserOrder
DROP TABLE IF EXISTS user_order CASCADE;
CREATE TABLE user_order (
    id SERIAL PRIMARY KEY,
    user_id INT,
    order_no VARCHAR(100),
    amount DECIMAL(10, 2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_order_user_id ON user_order(user_id);

-- ProductVector (PGVector extension support - fallback to REAL[] if not available)
DROP TABLE IF EXISTS product_vector CASCADE;
CREATE TABLE product_vector (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    embedding REAL[]  -- Or use vector(128) if pgvector extension is installed
);

-- ComplexOrder (Level 5 - JSONB native support)
DROP TABLE IF EXISTS complex_order CASCADE;
CREATE TABLE complex_order (
    id SERIAL PRIMARY KEY,
    order_no VARCHAR(100),
    address JSONB,
    items JSONB
);

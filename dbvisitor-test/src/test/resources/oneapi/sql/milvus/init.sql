DROP TABLE IF EXISTS product_vector;
CREATE TABLE IF NOT EXISTS product_vector (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(100),
    embedding FLOAT_VECTOR(128)
) WITH (consistency_level = 'Strong');

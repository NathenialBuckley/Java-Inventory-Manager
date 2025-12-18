-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Create items table
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    sku VARCHAR(255),
    quantity INTEGER,
    price DECIMAL(19,2),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    user_id BIGINT,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    quantity INTEGER NOT NULL,
    price_per_unit DECIMAL(19,2) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    inventory_before INTEGER,
    inventory_after INTEGER,
    notes VARCHAR(1000),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Campasian users table
CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    full_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    ein_number  VARCHAR(50)  NOT NULL,
    university_name VARCHAR(255) NOT NULL,
    department  VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_ein ON users(ein_number);

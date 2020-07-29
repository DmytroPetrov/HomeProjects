DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,
    first_name   TEXT       NOT NULL,
    last_name    TEXT       NOT NULL,
    email       TEXT UNIQUE NOT NULL,
    password    TEXT        NOT NULL,
    active      BOOLEAN DEFAULT FALSE,
    created     TIMESTAMP DEFAULT now(),
    last_updated TIMESTAMP DEFAULT now()
);
DROP TABLE IF EXISTS email_token;
CREATE TABLE email_token
(
    user_id         BIGINT REFERENCES users ON DELETE CASCADE,
    token           TEXT    UNIQUE NOT NULL,
    created         TIMESTAMP DEFAULT now()
);
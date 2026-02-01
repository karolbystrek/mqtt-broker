CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL
);

CREATE TABLE permissions
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users (id) ON DELETE CASCADE,
    topic        VARCHAR(255) NOT NULL,
    access_level VARCHAR(20)  NOT NULL CHECK (access_level IN ('READ', 'WRITE', 'READ_WRITE'))
);

-- Initial Data
INSERT INTO users (username, password)
VALUES ('user', 'password');

INSERT INTO permissions (user_id, topic, access_level)
VALUES ((SELECT id FROM users WHERE username = 'user'), 'readwrite/#', 'READ_WRITE');

INSERT INTO permissions (user_id, topic, access_level)
VALUES ((SELECT id FROM users WHERE username = 'user'), 'readonly/#', 'READ');

INSERT INTO permissions (user_id, topic, access_level)
VALUES ((SELECT id FROM users WHERE username = 'user'), 'writeonly/#', 'WRITE');

INSERT INTO users (username, password)
VALUES ('john', 'doe');

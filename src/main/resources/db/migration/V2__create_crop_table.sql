CREATE TABLE crop
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    variety     VARCHAR(255) NOT NULL,
    is_priority BOOLEAN      NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP
);

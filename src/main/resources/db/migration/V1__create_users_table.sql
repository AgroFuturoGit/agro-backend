CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    cpf           VARCHAR(14)  NOT NULL UNIQUE,
    date_of_birth DATE,
    role          VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP,
    deleted_at    TIMESTAMP
);

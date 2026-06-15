CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE communities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    organization_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_community_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE managers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    organization_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_manager_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_manager_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE producers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    community_id UUID NOT NULL,
    alias_name VARCHAR(255),
    is_compliant BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_producer_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_producer_community FOREIGN KEY (community_id) REFERENCES communities(id)
);

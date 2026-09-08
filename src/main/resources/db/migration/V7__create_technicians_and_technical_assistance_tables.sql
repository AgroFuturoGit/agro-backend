CREATE TABLE technicians (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    professional_id VARCHAR(255),
    specialty VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_technician_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE technical_assistances (
    id UUID PRIMARY KEY,
    technician_id UUID NOT NULL,
    farmer_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_technical_assistance_technician FOREIGN KEY (technician_id) REFERENCES technicians(id),
    CONSTRAINT fk_technical_assistance_farmer FOREIGN KEY (farmer_id) REFERENCES farmers(id)
);

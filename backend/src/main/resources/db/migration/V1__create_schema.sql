CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('MEMBER', 'EDITOR', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE components (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN
        ('CPU', 'GPU', 'MOTHERBOARD', 'RAM', 'STORAGE', 'PSU', 'CASE', 'COOLER')),
    manufacturer VARCHAR(80) NOT NULL,
    model VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    power_watts INTEGER CHECK (power_watts IS NULL OR power_watts BETWEEN 0 AND 2000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_component_identity UNIQUE (manufacturer, model, category)
);

CREATE TABLE configurations (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    budget_eur DECIMAL(10,2) NOT NULL CHECK (budget_eur BETWEEN 1 AND 100000),
    usage_type VARCHAR(30) NOT NULL CHECK (usage_type IN ('GAMING', 'WORKSTATION', 'OFFICE', 'OTHER')),
    visibility VARCHAR(20) NOT NULL CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE offers (
    id UUID PRIMARY KEY,
    component_id UUID NOT NULL REFERENCES components(id) ON DELETE CASCADE,
    supplier_name VARCHAR(100) NOT NULL,
    price_eur DECIMAL(10,2) NOT NULL CHECK (price_eur > 0),
    product_url VARCHAR(500) NOT NULL,
    stock_status VARCHAR(30) NOT NULL CHECK (stock_status IN ('IN_STOCK', 'LOW_STOCK', 'OUT_OF_STOCK')),
    checked_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_offer_component_supplier UNIQUE (component_id, supplier_name)
);

CREATE TABLE configuration_components (
    configuration_id UUID NOT NULL REFERENCES configurations(id) ON DELETE CASCADE,
    component_id UUID NOT NULL REFERENCES components(id) ON DELETE RESTRICT,
    added_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (configuration_id, component_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_refresh_replacement FOREIGN KEY (replaced_by_id) REFERENCES refresh_tokens(id) ON DELETE SET NULL
);


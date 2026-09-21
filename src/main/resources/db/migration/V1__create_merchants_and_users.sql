-- Marchands
CREATE TABLE merchants (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    logo_url            VARCHAR(500),
    primary_color       VARCHAR(20),
    secondary_color     VARCHAR(20),
    description         TEXT,
    google_review_url   VARCHAR(500),
    active              BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

-- Utilisateurs (champs communs uniquement)
CREATE TABLE users (
    id                  UUID PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    first_name          VARCHAR(255) NOT NULL,
    last_name           VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    email_verified      BOOLEAN NOT NULL DEFAULT false,
    active              BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

-- Extension 1-1 : profil client
-- La présence d'une ligne ici signifie que l'utilisateur est un Customer
CREATE TABLE customer_profiles (
    user_id     UUID PRIMARY KEY REFERENCES users(id),
    phone       VARCHAR(20),
    birth_date  DATE
);

-- Extension 1-1 : gestionnaire de marchand
-- La présence d'une ligne ici signifie que l'utilisateur est un MerchantManager
CREATE TABLE merchant_managers (
    user_id      UUID PRIMARY KEY REFERENCES users(id),
    merchant_id  UUID NOT NULL REFERENCES merchants(id)
);

CREATE INDEX idx_merchant_managers_merchant_id ON merchant_managers(merchant_id);

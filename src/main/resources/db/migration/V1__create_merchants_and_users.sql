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

-- Utilisateurs
-- Contient uniquement les informations communes au compte utilisateur.
CREATE TABLE users (
                       id              UUID PRIMARY KEY,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       first_name      VARCHAR(255) NOT NULL,
                       last_name       VARCHAR(255) NOT NULL,
                       email_verified  BOOLEAN NOT NULL DEFAULT false,
                       active          BOOLEAN NOT NULL DEFAULT true,
                       created_at      TIMESTAMP NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- Identités d'authentification
-- Un utilisateur peut posséder plusieurs identités
-- (email/password, Google, Apple, etc.).
CREATE TABLE auth_identities (
                                 id                UUID PRIMARY KEY,
                                 user_id           UUID NOT NULL REFERENCES users(id),
                                 provider          VARCHAR(50) NOT NULL,
                                 provider_user_id  VARCHAR(255),
                                 password_hash     VARCHAR(255),
                                 created_at        TIMESTAMP NOT NULL DEFAULT now(),

                                 CONSTRAINT uk_auth_identity_provider_user
                                     UNIQUE (provider, provider_user_id)

);

CREATE INDEX idx_auth_identities_user_id
    ON auth_identities(user_id);

-- Un utilisateur ne peut avoir qu'une seule identité
-- utilisant l'authentification par e-mail.
CREATE UNIQUE INDEX uk_auth_identity_user_email
    ON auth_identities(user_id)
    WHERE provider = 'email';

-- Extension 1-1 : profil client
-- La présence d'une ligne ici signifie que l'utilisateur est un Customer.
CREATE TABLE customer_profiles (
                                   user_id     UUID PRIMARY KEY REFERENCES users(id),
                                   phone       VARCHAR(20),
                                   birth_date  DATE
);

-- Extension 1-1 : gestionnaire de marchand
-- La présence d'une ligne ici signifie que l'utilisateur est un MerchantManager.
CREATE TABLE merchant_managers (
                                   user_id      UUID PRIMARY KEY REFERENCES users(id),
                                   merchant_id  UUID NOT NULL REFERENCES merchants(id)
);

CREATE INDEX idx_merchant_managers_merchant_id
    ON merchant_managers(merchant_id);

-- Sessions de refresh token
-- Seul le hash du refresh token est stocké en base.
-- family_id permet de révoquer toute une famille de tokens
-- en cas de détection d'une réutilisation d'un token déjà consommé.
CREATE TABLE refresh_sessions (
                                  id           UUID PRIMARY KEY,
                                  user_id      UUID NOT NULL REFERENCES users(id),
                                  family_id    UUID NOT NULL,
                                  token_hash   VARCHAR(255) NOT NULL UNIQUE,
                                  expires_at   TIMESTAMP NOT NULL,
                                  revoked_at   TIMESTAMP,
                                  created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_sessions_user_id
    ON refresh_sessions(user_id);

CREATE INDEX idx_refresh_sessions_family_id
    ON refresh_sessions(family_id);

-- Tokens temporaires de réinitialisation du mot de passe
-- Seul le hash du token envoyé par e-mail est stocké en base.
CREATE TABLE password_reset_tokens (
                                       id           UUID PRIMARY KEY,
                                       user_id      UUID NOT NULL REFERENCES users(id),
                                       token_hash   VARCHAR(255) NOT NULL UNIQUE,
                                       expires_at   TIMESTAMP NOT NULL,
                                       used_at      TIMESTAMP,
                                       created_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens(user_id);

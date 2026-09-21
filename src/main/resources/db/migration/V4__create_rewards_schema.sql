-- Récompenses proposées par un marchand
CREATE TABLE rewards (
    id                  UUID PRIMARY KEY,
    merchant_id         UUID NOT NULL REFERENCES merchants(id),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    cost_points         INTEGER NOT NULL,
    quantity_available  INTEGER,
    valid_from          TIMESTAMP,
    valid_to            TIMESTAMP,
    active              BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_rewards_merchant_id ON rewards(merchant_id);

-- Utilisation d'une récompense par un client
CREATE TABLE reward_redemptions (
    id              UUID PRIMARY KEY,
    reward_id       UUID NOT NULL REFERENCES rewards(id),
    loyalty_id      UUID NOT NULL REFERENCES loyalties(id),
    qr_token        VARCHAR(255) NOT NULL UNIQUE,
    -- status: 0 = PENDING, 1 = CONSUMED, 2 = EXPIRED, 3 = CANCELLED
    status          SMALLINT NOT NULL CHECK (status IN (0, 1, 2, 3)),
    redeemed_at     TIMESTAMP,
    expires_at      TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_reward_redemptions_reward_id ON reward_redemptions(reward_id);
CREATE INDEX idx_reward_redemptions_loyalty_id ON reward_redemptions(loyalty_id);

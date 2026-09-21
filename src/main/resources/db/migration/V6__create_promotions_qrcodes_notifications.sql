-- Promotions proposées par un marchand
CREATE TABLE promotions (
    id              UUID PRIMARY KEY,
    merchant_id     UUID NOT NULL REFERENCES merchants(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    rule_json       TEXT,
    start_date      TIMESTAMP,
    end_date        TIMESTAMP,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_promotions_merchant_id ON promotions(merchant_id);

-- QR codes d'un marchand
CREATE TABLE qr_codes (
    id                      UUID PRIMARY KEY,
    merchant_id             UUID NOT NULL REFERENCES merchants(id),
    -- type: 0 = MAIN, 1 = LOYALTY, 2 = PROMOTION, 3 = REWARD, 4 = WHEEL, 5 = TICKET
    type                    SMALLINT NOT NULL CHECK (type IN (0, 1, 2, 3, 4, 5)),
    code                    VARCHAR(255) NOT NULL UNIQUE,
    target_reference_id     UUID,
    created_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_qr_codes_merchant_id ON qr_codes(merchant_id);

-- Notifications envoyées aux utilisateurs
CREATE TABLE notifications (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id),
    -- type: 0 = NEW_REWARD, 1 = POINTS_EARNED, 2 = PROMOTION, 3 = BIRTHDAY, 4 = SPECIAL_OFFER
    type        SMALLINT NOT NULL CHECK (type IN (0, 1, 2, 3, 4)),
    title       VARCHAR(255) NOT NULL,
    message     TEXT,
    is_read     BOOLEAN NOT NULL DEFAULT false,
    sent_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);

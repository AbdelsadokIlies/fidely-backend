-- Roues de récompenses d'un marchand
CREATE TABLE wheels (
    id                              UUID PRIMARY KEY,
    merchant_id                     UUID NOT NULL REFERENCES merchants(id),
    name                            VARCHAR(255) NOT NULL,
    active                          BOOLEAN NOT NULL DEFAULT true,
    min_interval_minutes            INTEGER NOT NULL DEFAULT 0,
    requires_validated_purchase     BOOLEAN NOT NULL DEFAULT false,
    created_at                      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_wheels_merchant_id ON wheels(merchant_id);

-- Lots pouvant être gagnés sur une roue
CREATE TABLE wheel_prizes (
    id                  UUID PRIMARY KEY,
    wheel_id            UUID NOT NULL REFERENCES wheels(id),
    label               VARCHAR(255) NOT NULL,
    probability_weight  INTEGER NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_wheel_prizes_wheel_id ON wheel_prizes(wheel_id);

-- Participations des clients aux roues
CREATE TABLE wheel_participations (
    id              UUID PRIMARY KEY,
    wheel_id        UUID NOT NULL REFERENCES wheels(id),
    customer_id     UUID NOT NULL REFERENCES users(id),
    ticket_id       UUID REFERENCES tickets(id),
    wheel_prize_id  UUID REFERENCES wheel_prizes(id),
    result_label    VARCHAR(255),
    played_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_wheel_participations_wheel_id ON wheel_participations(wheel_id);
CREATE INDEX idx_wheel_participations_customer_id ON wheel_participations(customer_id);
CREATE INDEX idx_wheel_participations_ticket_id ON wheel_participations(ticket_id);

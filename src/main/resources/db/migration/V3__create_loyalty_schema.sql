-- Relation de fidélité client/marchand
CREATE TABLE loyalties (
    id              UUID PRIMARY KEY,
    customer_id     UUID NOT NULL REFERENCES users(id),
    merchant_id     UUID NOT NULL REFERENCES merchants(id),
    points_balance  INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (customer_id, merchant_id)
);

CREATE INDEX idx_loyalties_customer_id ON loyalties(customer_id);
CREATE INDEX idx_loyalties_merchant_id ON loyalties(merchant_id);

-- Historique des mouvements de points
CREATE TABLE loyalty_transactions (
    id              UUID PRIMARY KEY,
    loyalty_id      UUID NOT NULL REFERENCES loyalties(id),
    ticket_id       UUID REFERENCES tickets(id),
    points          INTEGER NOT NULL,
    description     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_loyalty_transactions_loyalty_id ON loyalty_transactions(loyalty_id);
CREATE INDEX idx_loyalty_transactions_ticket_id ON loyalty_transactions(ticket_id);

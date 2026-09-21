-- Règles de calcul de points d'un marchand
CREATE TABLE point_rules (
    id                          UUID PRIMARY KEY,
    merchant_id                 UUID NOT NULL REFERENCES merchants(id),
    points_per_currency_unit    NUMERIC(10,4) NOT NULL,
    -- rounding_method: 0 = FLOOR, 1 = ROUND, 2 = CEIL
    rounding_method             SMALLINT NOT NULL CHECK (rounding_method IN (0, 1, 2)),
    active                      BOOLEAN NOT NULL DEFAULT true,
    valid_from                  TIMESTAMP NOT NULL,
    valid_to                    TIMESTAMP,
    created_at                  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_point_rules_merchant_id ON point_rules(merchant_id);

-- Tickets de caisse
CREATE TABLE tickets (
    id                  UUID PRIMARY KEY,
    merchant_id         UUID NOT NULL REFERENCES merchants(id),
    customer_id         UUID NOT NULL REFERENCES users(id),
    ticket_number       VARCHAR(100) NOT NULL,
    fingerprint_hash    VARCHAR(255) NOT NULL UNIQUE,
    ticket_date         DATE NOT NULL,
    ticket_time         TIME NOT NULL,
    amount              NUMERIC(10,2) NOT NULL,
    raw_ocr_text        TEXT,
    image_storage_ref   VARCHAR(500),
    -- status: 0 = PENDING, 1 = VALIDATED, 2 = REJECTED, 3 = DUPLICATE
    status              SMALLINT NOT NULL CHECK (status IN (0, 1, 2, 3)),
    rejection_reason    VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_tickets_merchant_id ON tickets(merchant_id);
CREATE INDEX idx_tickets_customer_id ON tickets(customer_id);
CREATE INDEX idx_tickets_ticket_number ON tickets(ticket_number);

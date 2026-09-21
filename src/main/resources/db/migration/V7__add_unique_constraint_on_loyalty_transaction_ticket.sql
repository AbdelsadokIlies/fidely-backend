ALTER TABLE loyalty_transactions
    ADD CONSTRAINT uk_loyalty_transaction_ticket
        UNIQUE (ticket_id);
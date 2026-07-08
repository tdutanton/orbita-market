ALTER TABLE order_outbox_events
    ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING',
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0,
    ADD COLUMN sent_at TIMESTAMPTZ;

UPDATE order_outbox_events
SET status = CASE
    WHEN sent = true THEN 'SENT'
    ELSE 'PENDING'
END;

DROP INDEX idx_order_outbox_sent;
ALTER TABLE order_outbox_events DROP COLUMN sent;

CREATE INDEX idx_order_outbox_status ON order_outbox_events(status);
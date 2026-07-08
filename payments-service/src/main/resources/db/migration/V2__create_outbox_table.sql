CREATE TABLE payment_outbox_events (
    id TEXT PRIMARY KEY,
    topic TEXT NOT NULL,
    key TEXT,
    payload TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_outbox_status ON payment_outbox_events(status);

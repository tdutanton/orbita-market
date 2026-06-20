CREATE TABLE accounts (
    user_id TEXT PRIMARY KEY UNIQUE,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE payment_inbox (
    event_id TEXT PRIMARY KEY UNIQUE,
    order_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_inbox_order_id ON payment_inbox(order_id);
CREATE INDEX idx_payment_inbox_status ON payment_inbox(status);

WITH user_stats AS (
    SELECT
        user_id,
        COUNT(*) AS paid_orders_count,
        SUM(price) AS total_spent_geocredits
    FROM orders
    WHERE status = 'PAID'
    GROUP BY user_id
)
SELECT
    ROW_NUMBER() OVER (ORDER BY total_spent_geocredits DESC) AS row_num,
    user_id,
    paid_orders_count,
    total_spent_geocredits
FROM user_stats
ORDER BY total_spent_geocredits DESC
LIMIT 10;
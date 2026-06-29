-- перейти в db:
-- docker compose exec orders-db psql -U postgres -d orders_db
-- кто и сколько купил (пользователь, количество заказов, сумма потраченных денег)
-- db - orders_db
-- table - orders
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

-- перейти в db:
-- docker compose exec orders-db psql -U postgres -d orders_db
-- сводка по типам заказов
-- (тип продукта - по типам количество заказов, количество покупателей, сумма потраченных денег, средняя цена, минимальная и максимальная цена
-- db - orders_db
-- table - orders
WITH product_sales AS (
    SELECT
        product_type,
        COUNT(*) AS total_orders,
        COUNT(DISTINCT user_id) AS unique_users,
        SUM(price) AS total_spent_geocredits,
        AVG(price) AS avg_price,
        MIN(price) AS min_price,
        MAX(price) AS max_price
    FROM orders
    WHERE status = 'PAID'
    GROUP BY product_type
)
SELECT
    ROW_NUMBER() OVER (ORDER BY total_spent_geocredits DESC) AS row_num,
    product_type,
    total_orders,
    unique_users,
    total_spent_geocredits,
    ROUND(avg_price, 2) AS avg_price,
    min_price,
    max_price
FROM product_sales
ORDER BY total_spent_geocredits DESC;

-- перейти в db:
-- docker compose exec payments-db psql -U postgres -d payments_db
-- данные по платежам (по статусам - количество платежей, сумма платежей, процент среди статусов)
-- db - payments_db
-- table - payment_inbox_events
WITH payment_summary AS (
    SELECT
        status,
        COUNT(*) AS payments_count,
        SUM(amount) AS total_spent_geocredits
    FROM payment_inbox_events
    GROUP BY status
)
SELECT
    status,
    payments_count,
    total_spent_geocredits,
    ROUND(payments_count * 100.0 / SUM(payments_count) OVER (), 2) AS percentage
FROM payment_summary
ORDER BY payments_count DESC;
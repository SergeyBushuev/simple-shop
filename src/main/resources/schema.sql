CREATE TABLE items (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    img_path    VARCHAR(255),
    price       DECIMAL(10, 2) NOT NULL,
    stock_count INT            NOT NULL DEFAULT 0
);

CREATE TABLE orders
(
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE order_items
(
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT         NOT NULL REFERENCES orders (id),
    item_id        BIGINT         NOT NULL REFERENCES items (id),
    quantity       INT            NOT NULL,
    CONSTRAINT unique_order_item UNIQUE (order_id, item_id)
);

CREATE TABLE cart_items
(
    id       BIGSERIAL PRIMARY KEY,
    item_id  BIGINT NOT NULL REFERENCES items (id),
    quantity INT    NOT NULL,
    CONSTRAINT unique_cart_item UNIQUE (item_id)
);
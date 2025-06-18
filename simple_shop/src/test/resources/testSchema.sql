CREATE TABLE items (
                       id          BIGSERIAL PRIMARY KEY,
                       title       VARCHAR(255)   NOT NULL,
                       description TEXT,
                       img_path    VARCHAR(255),
                       price       DECIMAL(10, 2) NOT NULL
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

INSERT INTO items (title, description, img_path, price)
VALUES ('Big cat','Big cat (Gigantic)','bigCat.jpg',1599.99),
('Medium cat','Medium cat (his size is normal)','mediumCat.jpg',799.04),
('Small cat','Small cat (very tiny)','smallCat.jpg',50.41),
('Funny cat','Funny cat (hilarius)','memeCat.jpg',999999.99),
('Marble dog','The best dog','mramorDog.jpg',70000.00),
('Big dog','Big doge (really huge dog)','bigDog.jpg',5000.00);

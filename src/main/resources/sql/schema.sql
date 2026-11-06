CREATE TABLE IF NOT EXISTS category
(
    id   INT AUTO_INCREMENT NOT NULL,
    name VARCHAR(30) COLLATE utf8mb4_bin NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS menu
(
    id            INT AUTO_INCREMENT NOT NULL,
    category_id   INT                NOT NULL,
    `description` VARCHAR(300)       NOT NULL,
    name          VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    price         INT                NOT NULL,
    available     BIT(1)             NOT NULL,
    CONSTRAINT pk_menu PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS orders
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    email         VARCHAR(100) COLLATE utf8mb4_bin NOT NULL,
    created_at    datetime(6)           NOT NULL,
    status        VARCHAR(16)           NOT NULL,
    address       VARCHAR(200)          NOT NULL,
    postcode      VARCHAR(5)            NOT NULL,
    memo          VARCHAR(200)          NULL,
    shipping_date date                  NOT NULL,
    cancelled_at  datetime(6)           NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS order_items
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    quantity   INT                   NOT NULL,
    menu_name  VARCHAR(100)          NOT NULL,
    price      INT                   NOT NULL,
    order_id   BIGINT                NOT NULL,
    menu_id    INT                   NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id)
    );

ALTER TABLE category
    ADD CONSTRAINT uc_category_name UNIQUE (name);

ALTER TABLE menu
    ADD CONSTRAINT FK_MENU_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_MENU FOREIGN KEY (menu_id) REFERENCES menu (id);

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id);
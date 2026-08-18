CREATE DATABASE IF NOT EXISTS pawnshop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pawnshop;

-- Пользователи (id_пользователя, логин, хеш_пароля, фио, роль)
CREATE TABLE IF NOT EXISTS users (
    id_user INT PRIMARY KEY AUTO_INCREMENT,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER'
);

-- Клиенты (id_клиента, фио, паспортные_данные, id_пользователя)
CREATE TABLE IF NOT EXISTS clients (
    id_client INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(150) NOT NULL,
    passport_data VARCHAR(100) NOT NULL UNIQUE,
    id_user INT NOT NULL UNIQUE,
    CONSTRAINT fk_clients_users
        FOREIGN KEY (id_user) REFERENCES users(id_user)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Договоры (id_договора, дата_выдачи, срок_возврата,
-- сумма_комиссии, сумма_выдачи, id_клиента)
CREATE TABLE IF NOT EXISTS contracts (
    id_contract INT PRIMARY KEY AUTO_INCREMENT,
    issue_date DATE NOT NULL,
    return_due_date DATE NOT NULL,
    commission_amount DECIMAL(12, 2) NOT NULL,
    loan_amount DECIMAL(12, 2) NOT NULL,
    id_client INT NOT NULL,
    CONSTRAINT chk_contract_dates CHECK (return_due_date >= issue_date),
    CONSTRAINT chk_commission_amount CHECK (commission_amount >= 0),
    CONSTRAINT chk_loan_amount CHECK (loan_amount > 0),
    CONSTRAINT fk_contracts_clients
        FOREIGN KEY (id_client) REFERENCES clients(id_client)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Товары (id_товара, название, текущий_статус)
CREATE TABLE IF NOT EXISTS items (
    id_item INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    current_status ENUM('PLEDGED', 'RETURNED', 'OWNED_BY_PAWNSHOP', 'SOLD')
        NOT NULL DEFAULT 'PLEDGED'
);

-- Состав_Договора (id_договора, id_товара, оценочная_стоимость)
CREATE TABLE IF NOT EXISTS contract_items (
    id_contract INT NOT NULL,
    id_item INT NOT NULL,
    assessed_value DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id_contract, id_item),
    CONSTRAINT chk_assessed_value CHECK (assessed_value > 0),
    CONSTRAINT fk_contract_items_contracts
        FOREIGN KEY (id_contract) REFERENCES contracts(id_contract)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_contract_items_items
        FOREIGN KEY (id_item) REFERENCES items(id_item)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Цены (id_цены, дата, значение, id_товара)
CREATE TABLE IF NOT EXISTS prices (
    id_price INT PRIMARY KEY AUTO_INCREMENT,
    price_date DATE NOT NULL,
    value DECIMAL(12, 2) NOT NULL,
    id_item INT NOT NULL,
    CONSTRAINT uq_prices_item_date UNIQUE (id_item, price_date),
    CONSTRAINT uq_prices_price_item UNIQUE (id_price, id_item),
    CONSTRAINT chk_price_value CHECK (value > 0),
    CONSTRAINT fk_prices_items
        FOREIGN KEY (id_item) REFERENCES items(id_item)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Продажи (id_продажи, дата_продажи, id_товара, id_цены)
CREATE TABLE IF NOT EXISTS sales (
    id_sale INT PRIMARY KEY AUTO_INCREMENT,
    sale_date DATE NOT NULL,
    id_item INT NOT NULL UNIQUE,
    id_price INT NOT NULL UNIQUE,
    CONSTRAINT fk_sales_items
        FOREIGN KEY (id_item) REFERENCES items(id_item)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_price_for_same_item
        FOREIGN KEY (id_price, id_item) REFERENCES prices(id_price, id_item)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Представление для одного из составных запросов приложения.
CREATE OR REPLACE VIEW financial_report AS
SELECT c.id_contract,
       cl.full_name AS client_name,
       c.issue_date,
       c.return_due_date,
       c.loan_amount,
       c.commission_amount,
       COALESCE(SUM(ci.assessed_value), 0) AS total_assessed_value,
       COUNT(ci.id_item) AS item_count
FROM contracts c
JOIN clients cl ON cl.id_client = c.id_client
LEFT JOIN contract_items ci ON ci.id_contract = c.id_contract
GROUP BY c.id_contract, cl.full_name, c.issue_date, c.return_due_date,
         c.loan_amount, c.commission_amount;

-- ============================================================
-- ATM Interface Database Schema
-- Oasis Infobyte Java Development Internship
-- ============================================================
-- ER Diagram (Text):
--
--  [USERS] 1 ----< [ACCOUNTS] 1 ----< [TRANSACTIONS]
--      |                                      |
--      |                                      |
--  user_id (PK)                         transaction_id (PK)
--  full_name                            account_number (FK)
--  email                                transaction_type
--  phone                                amount
--  created_at                           balance_after
--                                       description
--  [ACCOUNTS]                           created_at
--  account_number (PK)
--  user_id (FK)
--  pin_hash
--  balance
--  account_type
--  status
--  created_at
--  last_login
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS atm_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE atm_db;

-- ============================================================
-- TABLE: users
-- Stores personal information for each bank customer
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(100)    NOT NULL,
    email         VARCHAR(150)    NOT NULL UNIQUE,
    phone         VARCHAR(15)     NOT NULL,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT chk_phone CHECK (phone REGEXP '^[0-9]{10,15}$')
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: accounts
-- Stores ATM account details linked to a user
-- ============================================================
CREATE TABLE IF NOT EXISTS accounts (
    account_number  VARCHAR(16)      NOT NULL,
    user_id         INT UNSIGNED     NOT NULL,
    pin_hash        VARCHAR(255)     NOT NULL COMMENT 'BCrypt hashed PIN',
    balance         DECIMAL(15,2)    NOT NULL DEFAULT 0.00,
    account_type    ENUM('SAVINGS','CURRENT','SALARY') NOT NULL DEFAULT 'SAVINGS',
    status          ENUM('ACTIVE','BLOCKED','CLOSED')  NOT NULL DEFAULT 'ACTIVE',
    failed_attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
    last_login      DATETIME         NULL,
    created_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_accounts      PRIMARY KEY (account_number),
    CONSTRAINT fk_account_user  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_balance      CHECK (balance >= 0.00)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: transactions
-- Immutable ledger of all ATM operations
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id    BIGINT UNSIGNED   NOT NULL AUTO_INCREMENT,
    account_number    VARCHAR(16)       NOT NULL,
    transaction_type  ENUM('DEPOSIT','WITHDRAWAL','TRANSFER_IN','TRANSFER_OUT','BALANCE_INQUIRY') NOT NULL,
    amount            DECIMAL(15,2)     NOT NULL,
    balance_after     DECIMAL(15,2)     NOT NULL,
    description       VARCHAR(255)      NULL,
    reference_number  VARCHAR(20)       NULL UNIQUE COMMENT 'Unique transaction reference',
    created_at        DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_transactions        PRIMARY KEY (transaction_id),
    CONSTRAINT fk_txn_account         FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE RESTRICT,
    CONSTRAINT chk_txn_amount         CHECK (amount > 0)
) ENGINE=InnoDB;

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_transactions_account   ON transactions(account_number);
CREATE INDEX idx_transactions_created   ON transactions(created_at);
CREATE INDEX idx_accounts_user          ON accounts(user_id);
CREATE INDEX idx_accounts_status        ON accounts(status);

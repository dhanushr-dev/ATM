-- ============================================================
-- ATM Interface - Sample Data
-- Oasis Infobyte Java Development Internship
-- ============================================================
-- NOTE: PINs are BCrypt hashed.
--   Account 1001000000000001 -> PIN: 1234
--   Account 1001000000000002 -> PIN: 5678
--   Account 1001000000000003 -> PIN: 9999
-- ============================================================

USE atm_db;

-- Clear existing data (order matters for FK constraints)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE transactions;
TRUNCATE TABLE accounts;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- Sample Users
-- ============================================================
INSERT INTO users (user_id, full_name, email, phone) VALUES
(1, 'Arjun Sharma',   'arjun.sharma@email.com',   '9876543210'),
(2, 'Priya Patel',    'priya.patel@email.com',    '9123456789'),
(3, 'Rahul Mehta',    'rahul.mehta@email.com',    '9988776655');

-- ============================================================
-- Sample Accounts (PIN hashes generated with BCrypt, cost=12)
-- ============================================================
INSERT INTO accounts (account_number, user_id, pin_hash, balance, account_type, status) VALUES
('1001000000000001', 1,
 '$2a$12$tYXFhr6fBXDWMOsOrclVo.vHLwVbpdhEbTSzggio4sqr6Ew/BII3.',
 25000.00, 'SAVINGS', 'ACTIVE'),

('1001000000000002', 2,
 '$2a$12$bSqi5G6yyV1MhY9xWJrrZeEZkzqjbsMDhDV3s1Vgj5YnMsb0riMrm',
 50000.00, 'CURRENT', 'ACTIVE'),

('1001000000000003', 3,
 '$2a$12$MjJzYYFnjG8XEb.1WeUxw.j1ke0lUgEZgb.WEBrSveRG8S8TYWKt2',
 10000.00, 'SAVINGS', 'ACTIVE');

-- ============================================================
-- Sample Transactions
-- ============================================================
INSERT INTO transactions (account_number, transaction_type, amount, balance_after, description, reference_number) VALUES
('1001000000000001', 'DEPOSIT',     10000.00, 25000.00, 'Initial deposit',        'TXN20240101001'),
('1001000000000001', 'WITHDRAWAL',   2000.00, 23000.00, 'ATM cash withdrawal',    'TXN20240102001'),
('1001000000000001', 'TRANSFER_OUT', 3000.00, 20000.00, 'Transfer to 1001000000000002', 'TXN20240103001'),
('1001000000000002', 'TRANSFER_IN',  3000.00, 53000.00, 'Transfer from 1001000000000001', 'TXN20240103002'),
('1001000000000002', 'DEPOSIT',     10000.00, 63000.00, 'Salary credit',          'TXN20240104001'),
('1001000000000002', 'WITHDRAWAL',  13000.00, 50000.00, 'ATM cash withdrawal',    'TXN20240105001'),
('1001000000000003', 'DEPOSIT',     10000.00, 10000.00, 'Initial deposit',        'TXN20240106001');

USE atm_db;

-- Reset failed attempts and unblock all accounts
UPDATE accounts SET failed_attempts = 0, status = 'ACTIVE';

-- Update PIN hashes with freshly generated correct BCrypt hashes
-- Account 1001000000000001 -> PIN: 1234
UPDATE accounts SET pin_hash = '$2a$12$tYXFhr6fBXDWMOsOrclVo.vHLwVbpdhEbTSzggio4sqr6Ew/BII3.'
  WHERE account_number = '1001000000000001';

-- Account 1001000000000002 -> PIN: 5678
UPDATE accounts SET pin_hash = '$2a$12$bSqi5G6yyV1MhY9xWJrrZeEZkzqjbsMDhDV3s1Vgj5YnMsb0riMrm'
  WHERE account_number = '1001000000000002';

-- Account 1001000000000003 -> PIN: 9999
UPDATE accounts SET pin_hash = '$2a$12$MjJzYYFnjG8XEb.1WeUxw.j1ke0lUgEZgb.WEBrSveRG8S8TYWKt2'
  WHERE account_number = '1001000000000003';

SELECT account_number, failed_attempts, status, LEFT(pin_hash, 20) AS hash_preview FROM accounts;

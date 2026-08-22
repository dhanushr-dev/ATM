# 🧪 Manual Test Cases — ATM Interface

## TC-001: Login — Valid Credentials
| Field | Value |
|---|---|
| Account Number | 1001000000000001 |
| PIN | 1234 |
| **Expected** | Dashboard opens, balance ₹25,000 displayed |
| **Status** | ⬜ Pass / ⬜ Fail |

## TC-002: Login — Invalid PIN
| Input | Account: 1001000000000001, PIN: 0000 |
|---|---|
| **Expected** | Error: "Incorrect PIN. 2 attempt(s) remaining." |
| **Status** | ⬜ |

## TC-003: Login — Account Locked After 3 Failed PINs
| Steps | Enter wrong PIN 3 times for same account |
|---|---|
| **Expected** | "Account blocked after too many failed attempts." |
| **Status** | ⬜ |

## TC-004: Login — Empty Fields
| Input | Both fields blank |
|---|---|
| **Expected** | "Please enter both Account Number and PIN." |
| **Status** | ⬜ |

## TC-005: Login — Non-existent Account
| Input | Account: 9999999999999999 |
|---|---|
| **Expected** | "Account not found: 9999999999999999" |
| **Status** | ⬜ |

## TC-006: Deposit — Valid Amount
| Input | ₹5,000 |
|---|---|
| **Expected** | Balance increases by 5000; success message with reference number |
| **Status** | ⬜ |

## TC-007: Deposit — Zero Amount
| Input | 0 |
|---|---|
| **Expected** | "Amount must be greater than zero." |
| **Status** | ⬜ |

## TC-008: Deposit — Exceeds Limit
| Input | 200000 |
|---|---|
| **Expected** | "Maximum single deposit is ₹100,000.00." |
| **Status** | ⬜ |

## TC-009: Deposit — Non-Numeric Input
| Input | "abc" |
|---|---|
| **Expected** | "Invalid amount format." |
| **Status** | ⬜ |

## TC-010: Deposit — Confirmation Cancel
| Steps | Enter 1000, click DEPOSIT, click NO in dialog |
|---|---|
| **Expected** | No transaction; balance unchanged |
| **Status** | ⬜ |

## TC-011: Withdrawal — Valid Amount
| Input | ₹2,000 from account with ₹25,000 |
|---|---|
| **Expected** | Balance decreases by 2000; success message |
| **Status** | ⬜ |

## TC-012: Withdrawal — Insufficient Funds
| Input | ₹30,000 from account with ₹25,000 |
|---|---|
| **Expected** | "Insufficient funds. Available: ₹25,000.00, Requested: ₹30,000.00" |
| **Status** | ⬜ |

## TC-013: Withdrawal — Exceeds Per-Transaction Limit
| Input | 60000 |
|---|---|
| **Expected** | "Maximum single withdrawal is ₹50,000.00." |
| **Status** | ⬜ |

## TC-014: Transfer — Valid Transfer
| Steps | Transfer ₹1,000 to 1001000000000002 |
|---|---|
| **Expected** | Source balance decreases; destination balance increases; both transactions recorded |
| **Status** | ⬜ |

## TC-015: Transfer — To Self
| Input | Dest = own account number |
|---|---|
| **Expected** | "Cannot transfer to the same account." |
| **Status** | ⬜ |

## TC-016: Transfer — Non-existent Destination
| Input | Dest = 0000000000000000 |
|---|---|
| **Expected** | "Account not found: 0000000000000000" |
| **Status** | ⬜ |

## TC-017: Transfer — Insufficient Funds
| Input | Transfer ₹50,000 from account with ₹25,000 |
|---|---|
| **Expected** | InsufficientFundsException with amounts shown |
| **Status** | ⬜ |

## TC-018: Balance Inquiry
| Steps | Navigate to Balance Inquiry |
|---|---|
| **Expected** | Correct balance displayed; inquiry recorded in history |
| **Status** | ⬜ |

## TC-019: Transaction History
| Steps | Perform deposit, withdraw, transfer; view history |
|---|---|
| **Expected** | All 3 transactions visible; correct types, amounts, balance-after |
| **Status** | ⬜ |

## TC-020: Mini Statement
| Steps | Navigate to Mini Statement |
|---|---|
| **Expected** | Last 5 transactions shown in receipt format |
| **Status** | ⬜ |

## TC-021: Change PIN — Valid
| Input | Current: 1234, New: 5678, Confirm: 5678 |
|---|---|
| **Expected** | PIN changed; auto-logout; can login with 5678 |
| **Status** | ⬜ |

## TC-022: Change PIN — Wrong Current PIN
| Input | Current: 0000 (wrong) |
|---|---|
| **Expected** | "Current PIN is incorrect." |
| **Status** | ⬜ |

## TC-023: Change PIN — Mismatch
| Input | New: 1111, Confirm: 2222 |
|---|---|
| **Expected** | "PINs do not match." |
| **Status** | ⬜ |

## TC-024: Change PIN — Same as Current
| Input | New PIN = old PIN |
|---|---|
| **Expected** | "New PIN must be different from the current PIN." |
| **Status** | ⬜ |

## TC-025: Logout Confirmation
| Steps | Click Logout, click YES |
|---|---|
| **Expected** | Returns to Login screen; session cleared |
| **Status** | ⬜ |

## TC-026: Exit Confirmation
| Steps | Click Exit, click YES |
|---|---|
| **Expected** | Application closes |
| **Status** | ⬜ |

## TC-027: Exit Confirmation — Cancel
| Steps | Click Exit, click NO |
|---|---|
| **Expected** | Remains on dashboard |
| **Status** | ⬜ |

## TC-028: Decimal Amount
| Input | Deposit 1500.50 |
|---|---|
| **Expected** | ₹1,500.50 deposited successfully |
| **Status** | ⬜ |

## TC-029: Quick Amount Buttons
| Steps | Click ₹1000 quick button on Deposit panel |
|---|---|
| **Expected** | Amount field filled with "1000" |
| **Status** | ⬜ |

## TC-030: Database Connection Failure
| Steps | Stop MySQL, start app |
|---|---|
| **Expected** | Dialog: "Cannot connect to the database." App does not crash |
| **Status** | ⬜ |

---

## Edge Cases Checklist
- [ ] Very long account number (>16 digits) — blocked by UI document filter
- [ ] PIN with letters — blocked by UI document filter
- [ ] SQL injection in account field — blocked by PreparedStatement
- [ ] Negative amount — blocked by validator
- [ ] Amount with 3+ decimal places — rejected
- [ ] Transfer ₹0.50 (valid, above minimum)
- [ ] Concurrent sessions (multiple windows) — each independently tracked
- [ ] Window close button (X) — triggers exit confirmation

---

## Bug Checklist
- [ ] BCrypt hash mismatch between stored and entered PIN
- [ ] Balance not refreshed in sidebar after transaction
- [ ] Transfer partial commit (one side committed, other failed) — should not happen with JDBC transaction
- [ ] Account locked but app still shows "X attempts remaining"
- [ ] Mini statement shows BALANCE_INQUIRY entries (by design — ₹1 dummy amount)
- [ ] Window not centered on secondary monitors

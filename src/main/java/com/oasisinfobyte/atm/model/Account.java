package com.oasisinfobyte.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an ATM account.
 * Maps directly to the {@code accounts} table in the database.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class Account {

    /** Possible account types. */
    public enum AccountType {
        SAVINGS, CURRENT, SALARY
    }

    /** Possible account statuses. */
    public enum AccountStatus {
        ACTIVE, BLOCKED, CLOSED
    }

    /** Maximum allowed consecutive failed PIN attempts before blocking. */
    public static final int MAX_FAILED_ATTEMPTS = 3;

    private String       accountNumber;
    private int          userId;
    private String       pinHash;
    private BigDecimal   balance;
    private AccountType  accountType;
    private AccountStatus status;
    private int          failedAttempts;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Account() {
        this.balance        = BigDecimal.ZERO;
        this.accountType    = AccountType.SAVINGS;
        this.status         = AccountStatus.ACTIVE;
        this.failedAttempts = 0;
    }

    public Account(String accountNumber, int userId, String pinHash,
                   BigDecimal balance, AccountType accountType) {
        this();
        this.accountNumber = accountNumber;
        this.userId        = userId;
        this.pinHash       = pinHash;
        this.balance       = balance;
        this.accountType   = accountType;
    }

    // -------------------------------------------------------------------------
    // Business helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this account is active and not blocked.
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }

    /**
     * Returns {@code true} if the failed-attempt count has reached the maximum.
     */
    public boolean isLocked() {
        return this.failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getAccountNumber()          { return accountNumber; }
    public void   setAccountNumber(String an) { this.accountNumber = an; }

    public int  getUserId()             { return userId; }
    public void setUserId(int userId)   { this.userId = userId; }

    public String getPinHash()              { return pinHash; }
    public void   setPinHash(String ph)     { this.pinHash = ph; }

    public BigDecimal getBalance()                 { return balance; }
    public void       setBalance(BigDecimal bal)   { this.balance = bal; }

    public AccountType getAccountType()               { return accountType; }
    public void        setAccountType(AccountType at) { this.accountType = at; }

    public AccountStatus getStatus()                  { return status; }
    public void          setStatus(AccountStatus s)   { this.status = s; }

    public int  getFailedAttempts()               { return failedAttempts; }
    public void setFailedAttempts(int fa)         { this.failedAttempts = fa; }

    public LocalDateTime getLastLogin()                   { return lastLogin; }
    public void          setLastLogin(LocalDateTime ll)   { this.lastLogin = ll; }

    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public void          setCreatedAt(LocalDateTime ca)   { this.createdAt = ca; }

    public LocalDateTime getUpdatedAt()                   { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime ua)   { this.updatedAt = ua; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(accountNumber, account.accountNumber);
    }

    @Override
    public int hashCode() { return Objects.hash(accountNumber); }

    @Override
    public String toString() {
        return "Account{accountNumber='" + accountNumber + "', balance=" + balance
                + ", status=" + status + '}';
    }
}

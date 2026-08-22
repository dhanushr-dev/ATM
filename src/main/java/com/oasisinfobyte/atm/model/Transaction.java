package com.oasisinfobyte.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single ATM transaction (immutable ledger entry).
 * Maps directly to the {@code transactions} table in the database.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class Transaction {

    /** All supported ATM transaction types. */
    public enum TransactionType {
        DEPOSIT        ("Deposit",         "+"),
        WITHDRAWAL     ("Withdrawal",      "-"),
        TRANSFER_IN    ("Transfer In",     "+"),
        TRANSFER_OUT   ("Transfer Out",    "-"),
        BALANCE_INQUIRY("Balance Inquiry", " ");

        private final String displayName;
        private final String sign;

        TransactionType(String displayName, String sign) {
            this.displayName = displayName;
            this.sign        = sign;
        }

        public String getDisplayName() { return displayName; }
        public String getSign()        { return sign; }
    }

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private long            transactionId;
    private String          accountNumber;
    private TransactionType transactionType;
    private BigDecimal      amount;
    private BigDecimal      balanceAfter;
    private String          description;
    private String          referenceNumber;
    private LocalDateTime   createdAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Transaction() {}

    public Transaction(String accountNumber, TransactionType transactionType,
                       BigDecimal amount, BigDecimal balanceAfter,
                       String description, String referenceNumber) {
        this.accountNumber   = accountNumber;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.balanceAfter    = balanceAfter;
        this.description     = description;
        this.referenceNumber = referenceNumber;
    }

    // -------------------------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted date string for display in the UI.
     */
    public String getFormattedDate() {
        return createdAt != null ? createdAt.format(DISPLAY_FORMATTER) : "N/A";
    }

    /**
     * Returns the signed amount string (e.g. "+1,000.00" or "-500.00").
     */
    public String getSignedAmountString() {
        return transactionType.getSign() +
               String.format("%,.2f", amount);
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public long            getTransactionId()               { return transactionId; }
    public void            setTransactionId(long id)        { this.transactionId = id; }

    public String          getAccountNumber()               { return accountNumber; }
    public void            setAccountNumber(String an)      { this.accountNumber = an; }

    public TransactionType getTransactionType()             { return transactionType; }
    public void            setTransactionType(TransactionType tt) { this.transactionType = tt; }

    public BigDecimal      getAmount()                      { return amount; }
    public void            setAmount(BigDecimal amount)     { this.amount = amount; }

    public BigDecimal      getBalanceAfter()                { return balanceAfter; }
    public void            setBalanceAfter(BigDecimal ba)   { this.balanceAfter = ba; }

    public String          getDescription()                 { return description; }
    public void            setDescription(String desc)      { this.description = desc; }

    public String          getReferenceNumber()             { return referenceNumber; }
    public void            setReferenceNumber(String ref)   { this.referenceNumber = ref; }

    public LocalDateTime   getCreatedAt()                   { return createdAt; }
    public void            setCreatedAt(LocalDateTime ca)   { this.createdAt = ca; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return transactionId == that.transactionId;
    }

    @Override
    public int hashCode() { return Objects.hash(transactionId); }

    @Override
    public String toString() {
        return "Transaction{id=" + transactionId + ", type=" + transactionType
                + ", amount=" + amount + ", ref='" + referenceNumber + "'}";
    }
}

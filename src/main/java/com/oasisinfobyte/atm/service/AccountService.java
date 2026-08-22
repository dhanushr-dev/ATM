package com.oasisinfobyte.atm.service;

import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.TransactionDAO;
import com.oasisinfobyte.atm.database.DatabaseConnection;
import com.oasisinfobyte.atm.exception.ATMException;
import com.oasisinfobyte.atm.exception.ATMException.ErrorCode;
import com.oasisinfobyte.atm.exception.AccountNotFoundException;
import com.oasisinfobyte.atm.exception.DatabaseException;
import com.oasisinfobyte.atm.exception.InsufficientFundsException;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.model.Transaction.TransactionType;
import com.oasisinfobyte.atm.utility.FormatUtil;
import com.oasisinfobyte.atm.utility.PasswordUtil;
import com.oasisinfobyte.atm.utility.ReferenceGenerator;
import com.oasisinfobyte.atm.validation.InputValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core banking service — handles all ATM operations:
 * deposit, withdrawal, transfer, balance inquiry, PIN change.
 *
 * <p>Transfer operations use explicit JDBC transactions (commit/rollback) to
 * guarantee atomicity: both the debit and credit must succeed together.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class AccountService {

    private static final Logger LOGGER = Logger.getLogger(AccountService.class.getName());

    /** Default number of entries for a mini-statement. */
    public static final int MINI_STATEMENT_LIMIT = 5;

    private final AccountDAO     accountDAO;
    private final TransactionDAO transactionDAO;
    private final AuthService    authService;

    public AccountService(AccountDAO accountDAO, TransactionDAO transactionDAO,
                          AuthService authService) {
        this.accountDAO     = accountDAO;
        this.transactionDAO = transactionDAO;
        this.authService    = authService;
    }

    // -------------------------------------------------------------------------
    // Balance Inquiry
    // -------------------------------------------------------------------------

    /**
     * Returns the current balance of the authenticated account.
     *
     * @return balance as {@link BigDecimal}
     */
    public BigDecimal getBalance() {
        // Bug fix: do NOT record a phantom ₹1 transaction for a balance check.
        // Simply refresh and return the live balance.
        authService.refreshCurrentAccount();
        return authService.getCurrentAccount().getBalance();
    }

    // -------------------------------------------------------------------------
    // Deposit
    // -------------------------------------------------------------------------

    /**
     * Deposits money into the authenticated account.
     *
     * @param amountStr the amount string entered by the user
     * @return the transaction record
     */
    public Transaction deposit(String amountStr) {
        BigDecimal amount  = InputValidator.validateDepositAmount(amountStr);
        Account    account = authService.getCurrentAccount();

        BigDecimal newBalance = account.getBalance().add(amount);

        Transaction txn = new Transaction(
                account.getAccountNumber(),
                TransactionType.DEPOSIT,
                amount,
                newBalance,
                "ATM cash deposit",
                ReferenceGenerator.generate()
        );

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(account.getAccountNumber(), newBalance, conn);
                transactionDAO.save(txn, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Deposit transaction failed", e);
            throw new DatabaseException("Deposit failed: " + e.getMessage(), e);
        }

        authService.refreshCurrentAccount();
        LOGGER.info("Deposit of " + FormatUtil.formatCurrency(amount) + " successful.");
        return txn;
    }

    // -------------------------------------------------------------------------
    // Withdrawal
    // -------------------------------------------------------------------------

    /**
     * Withdraws money from the authenticated account.
     *
     * @param amountStr the amount string entered by the user
     * @return the transaction record
     * @throws InsufficientFundsException if balance is insufficient
     */
    public Transaction withdraw(String amountStr) {
        BigDecimal amount  = InputValidator.validateWithdrawalAmount(amountStr);
        Account    account = authService.getCurrentAccount();

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        Transaction txn = new Transaction(
                account.getAccountNumber(),
                TransactionType.WITHDRAWAL,
                amount,
                newBalance,
                "ATM cash withdrawal",
                ReferenceGenerator.generate()
        );

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(account.getAccountNumber(), newBalance, conn);
                transactionDAO.save(txn, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Withdrawal transaction failed", e);
            throw new DatabaseException("Withdrawal failed: " + e.getMessage(), e);
        }

        authService.refreshCurrentAccount();
        LOGGER.info("Withdrawal of " + FormatUtil.formatCurrency(amount) + " successful.");
        return txn;
    }

    // -------------------------------------------------------------------------
    // Transfer
    // -------------------------------------------------------------------------

    /**
     * Transfers money from the authenticated account to a destination account.
     *
     * <p>The entire operation (debit + credit + 2 transaction records) runs
     * inside a single JDBC transaction.</p>
     *
     * @param destinationAccountNumber the recipient's 16-digit account number
     * @param amountStr                the amount string entered by the user
     * @return the outgoing transaction record
     */
    public Transaction transfer(String destinationAccountNumber, String amountStr) {
        // Validate inputs
        InputValidator.validateAccountNumber(destinationAccountNumber);
        BigDecimal amount       = InputValidator.validateTransferAmount(amountStr);
        Account    sourceAccount = authService.getCurrentAccount();

        // Guard: cannot transfer to self
        if (sourceAccount.getAccountNumber().equals(destinationAccountNumber)) {
            throw new ATMException(ErrorCode.TRANSFER_TO_SELF,
                    "Cannot transfer to the same account.");
        }

        // Fetch destination
        Account destAccount = accountDAO.findByAccountNumber(destinationAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException(destinationAccountNumber));

        if (!destAccount.isActive()) {
            throw new ATMException(ErrorCode.DESTINATION_ACCOUNT_NOT_FOUND,
                    "Destination account is not active.");
        }

        // Check funds
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(sourceAccount.getBalance(), amount);
        }

        BigDecimal sourceNewBalance = sourceAccount.getBalance().subtract(amount);
        BigDecimal destNewBalance   = destAccount.getBalance().add(amount);

        String refOut = ReferenceGenerator.generate();
        String refIn  = ReferenceGenerator.generate();

        Transaction txnOut = new Transaction(sourceAccount.getAccountNumber(),
                TransactionType.TRANSFER_OUT, amount, sourceNewBalance,
                "Transfer to " + destinationAccountNumber, refOut);

        Transaction txnIn = new Transaction(destAccount.getAccountNumber(),
                TransactionType.TRANSFER_IN, amount, destNewBalance,
                "Transfer from " + sourceAccount.getAccountNumber(), refIn);

        // Execute atomically
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(sourceAccount.getAccountNumber(), sourceNewBalance, conn);
                accountDAO.updateBalance(destAccount.getAccountNumber(),   destNewBalance,   conn);
                transactionDAO.save(txnOut, conn);
                transactionDAO.save(txnIn,  conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Transfer transaction failed", e);
            throw new DatabaseException("Transfer failed: " + e.getMessage(), e);
        }

        authService.refreshCurrentAccount();
        LOGGER.info("Transfer of " + FormatUtil.formatCurrency(amount)
                + " to " + destinationAccountNumber + " successful.");
        return txnOut;
    }

    // -------------------------------------------------------------------------
    // PIN Change
    // -------------------------------------------------------------------------

    /**
     * Changes the PIN of the authenticated account.
     *
     * @param currentPin the user's current PIN (for re-authentication)
     * @param newPin     the desired new PIN
     * @param confirmPin the confirmation of the new PIN
     */
    public void changePin(String currentPin, String newPin, String confirmPin) {
        Account account = authService.getCurrentAccount();

        // Verify current PIN
        if (!PasswordUtil.verifyPin(currentPin, account.getPinHash())) {
            throw new ATMException(ErrorCode.INVALID_CREDENTIALS,
                    "Current PIN is incorrect.");
        }

        // Validate new PIN
        InputValidator.validatePinMatch(newPin, confirmPin);

        // New PIN must differ from current
        if (PasswordUtil.verifyPin(newPin, account.getPinHash())) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR,
                    "New PIN must be different from the current PIN.");
        }

        String newHash = PasswordUtil.hashPin(newPin);
        accountDAO.updatePin(account.getAccountNumber(), newHash);
        authService.refreshCurrentAccount();

        LOGGER.info("PIN changed successfully for account: " + account.getAccountNumber());
    }

    // -------------------------------------------------------------------------
    // Transaction History & Mini Statement
    // -------------------------------------------------------------------------

    /**
     * Returns the complete transaction history for the authenticated account.
     *
     * @return list of all transactions, newest first
     */
    public List<Transaction> getTransactionHistory() {
        Account account = authService.getCurrentAccount();
        return transactionDAO.findByAccountNumber(account.getAccountNumber());
    }

    /**
     * Returns the last {@value #MINI_STATEMENT_LIMIT} transactions.
     *
     * @return list of recent transactions
     */
    public List<Transaction> getMiniStatement() {
        Account account = authService.getCurrentAccount();
        return transactionDAO.findRecentByAccountNumber(
                account.getAccountNumber(), MINI_STATEMENT_LIMIT);
    }

    // -------------------------------------------------------------------------
    // Private helpers  (recordBalanceInquiry removed — no phantom transactions)
    // -------------------------------------------------------------------------
}

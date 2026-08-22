package com.oasisinfobyte.atm.controller;

import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.service.AccountService;
import com.oasisinfobyte.atm.service.AuthService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Central controller for the ATM Interface application.
 *
 * <p>Acts as the single point of contact between the Swing UI layer and the
 * business-logic service layer. The UI never calls DAO or service classes
 * directly — it always goes through this controller (MVC pattern).</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class ATMController {

    private final AuthService    authService;
    private final AccountService accountService;

    public ATMController(AuthService authService, AccountService accountService) {
        this.authService    = authService;
        this.accountService = accountService;
    }

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    /**
     * Authenticates the user.
     *
     * @param accountNumber the 16-digit account number
     * @param pin           the 4-digit PIN
     * @return the authenticated {@link Account}
     */
    public Account login(String accountNumber, String pin) {
        return authService.login(accountNumber, pin);
    }

    /**
     * Registers a new account.
     */
    public Account registerAccount(String fullName, String email, String phone,
                                   String pin, java.math.BigDecimal initialDeposit,
                                   Account.AccountType accountType) {
        return authService.registerAccount(fullName, email, phone, pin, initialDeposit, accountType);
    }

    /**
     * Logs out the current user, clearing the session.
     */
    public void logout() {
        authService.logout();
    }

    /**
     * Returns the currently logged-in account.
     */
    public Account getCurrentAccount() {
        return authService.getCurrentAccount();
    }

    /**
     * Returns the currently logged-in user's profile.
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    // -------------------------------------------------------------------------
    // Banking operations
    // -------------------------------------------------------------------------

    /**
     * Deposits the given amount into the current account.
     *
     * @param amountStr string entered in the UI
     * @return the recorded {@link Transaction}
     */
    public Transaction deposit(String amountStr) {
        return accountService.deposit(amountStr);
    }

    /**
     * Withdraws the given amount from the current account.
     *
     * @param amountStr string entered in the UI
     * @return the recorded {@link Transaction}
     */
    public Transaction withdraw(String amountStr) {
        return accountService.withdraw(amountStr);
    }

    /**
     * Transfers money from the current account to a destination account.
     *
     * @param destinationAccountNumber the recipient's account number
     * @param amountStr                string entered in the UI
     * @return the outgoing {@link Transaction}
     */
    public Transaction transfer(String destinationAccountNumber, String amountStr) {
        return accountService.transfer(destinationAccountNumber, amountStr);
    }

    /**
     * Returns the current balance of the authenticated account.
     *
     * @return balance as {@link BigDecimal}
     */
    public BigDecimal getBalance() {
        return accountService.getBalance();
    }

    /**
     * Changes the PIN of the authenticated account.
     *
     * @param currentPin the user's existing PIN
     * @param newPin     the desired new PIN
     * @param confirmPin the confirmation of the new PIN
     */
    public void changePin(String currentPin, String newPin, String confirmPin) {
        accountService.changePin(currentPin, newPin, confirmPin);
    }

    /**
     * Returns the full transaction history for the authenticated account.
     *
     * @return list of all transactions, newest first
     */
    public List<Transaction> getTransactionHistory() {
        return accountService.getTransactionHistory();
    }

    /**
     * Returns the last {@code N} transactions for the authenticated account
     * (mini statement).
     *
     * @return list of recent transactions
     */
    public List<Transaction> getMiniStatement() {
        return accountService.getMiniStatement();
    }
}

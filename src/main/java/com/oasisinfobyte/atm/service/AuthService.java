package com.oasisinfobyte.atm.service;

import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.UserDAO;
import com.oasisinfobyte.atm.exception.ATMException;
import com.oasisinfobyte.atm.exception.ATMException.ErrorCode;
import com.oasisinfobyte.atm.exception.AccountNotFoundException;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.Account.AccountStatus;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.utility.PasswordUtil;
import com.oasisinfobyte.atm.validation.InputValidator;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service responsible for ATM authentication and session management.
 *
 * <p>Enforces PIN verification, account-locking after repeated failures,
 * and maintains the currently authenticated account in a session context.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    private final AccountDAO accountDAO;
    private final UserDAO    userDAO;

    // Session state (cleared on logout)
    private Account currentAccount;
    private User    currentUser;

    public AuthService(AccountDAO accountDAO, UserDAO userDAO) {
        this.accountDAO = accountDAO;
        this.userDAO    = userDAO;
    }

    // -------------------------------------------------------------------------
    // Authentication
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user by account number and PIN.
     *
     * <p>After {@link Account#MAX_FAILED_ATTEMPTS} consecutive failures the
     * account is automatically blocked to prevent brute-force attacks.</p>
     *
     * @param accountNumber the 16-digit account number
     * @param pin           the 4-digit PIN
     * @return the authenticated {@link Account}
     * @throws ATMException if credentials are invalid or account is blocked
     */
    public Account login(String accountNumber, String pin) {
        // 1. Validate input format first
        InputValidator.validateAccountNumber(accountNumber);
        InputValidator.validatePin(pin);

        // 2. Fetch account
        Account account = accountDAO.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        // 3. Check account status
        if (AccountStatus.BLOCKED.equals(account.getStatus())) {
            throw new ATMException(ErrorCode.ACCOUNT_BLOCKED,
                    "Your account is blocked. Please contact your bank.");
        }
        if (AccountStatus.CLOSED.equals(account.getStatus())) {
            throw new ATMException(ErrorCode.ACCOUNT_BLOCKED,
                    "This account is closed.");
        }

        // 4. Verify PIN
        if (!PasswordUtil.verifyPin(pin, account.getPinHash())) {
            accountDAO.incrementFailedAttempts(accountNumber);

            // Re-fetch to get updated counter
            account = accountDAO.findByAccountNumber(accountNumber).orElseThrow();

            if (account.isLocked()) {
                accountDAO.updateStatus(accountNumber, AccountStatus.BLOCKED);
                throw new ATMException(ErrorCode.ACCOUNT_LOCKED,
                        "Account blocked after too many failed attempts. Contact your bank.");
            }

            int remaining = Account.MAX_FAILED_ATTEMPTS - account.getFailedAttempts();
            throw new ATMException(ErrorCode.INVALID_CREDENTIALS,
                    "Incorrect PIN. " + remaining + " attempt(s) remaining.");
        }

        // 5. Successful login — reset counter and set session
        accountDAO.resetFailedAttempts(accountNumber);

        // Reload fresh account state
        this.currentAccount = accountDAO.findByAccountNumber(accountNumber).orElseThrow();

        // Load associated user
        Optional<User> user = userDAO.findById(this.currentAccount.getUserId());
        this.currentUser = user.orElse(null);

        LOGGER.info("Login successful for account: " + accountNumber);
        return this.currentAccount;
    }

    /**
     * Registers a new user and creates an active bank account.
     */
    public Account registerAccount(String fullName, String email, String phone,
                                   String pin, java.math.BigDecimal initialDeposit,
                                   Account.AccountType accountType) {
        if (fullName == null || fullName.isBlank()) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR, "Full Name is required.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR, "Valid Email Address is required.");
        }
        if (phone == null || phone.isBlank() || !phone.matches("\\d{10}")) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR, "Mobile Phone must be 10 digits.");
        }
        InputValidator.validatePin(pin);
        if (initialDeposit == null || initialDeposit.compareTo(new java.math.BigDecimal("500.00")) < 0) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR, "Minimum initial deposit is ₹500.00.");
        }

        // Save User
        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone.trim());
        user = userDAO.save(user);

        // Generate 16-digit Account Number starting with 1001
        String accountNumber;
        java.util.Random random = new java.util.Random();
        do {
            long randomDigits = (long) (random.nextDouble() * 900000000000L) + 100000000000L;
            accountNumber = "1001" + randomDigits;
        } while (accountDAO.findByAccountNumber(accountNumber).isPresent());

        // Save Account
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUserId(user.getUserId());
        account.setPinHash(PasswordUtil.hashPin(pin));
        account.setBalance(initialDeposit);
        account.setAccountType(accountType != null ? accountType : Account.AccountType.SAVINGS);
        account.setStatus(AccountStatus.ACTIVE);
        account.setFailedAttempts(0);
        accountDAO.save(account);

        LOGGER.info("Registered new account: " + accountNumber + " for " + fullName);
        return account;
    }

    /**
     * Clears the session, effectively logging out the current user.
     */
    public void logout() {
        LOGGER.info("Logout: " + (currentAccount != null ? currentAccount.getAccountNumber() : "none"));
        this.currentAccount = null;
        this.currentUser    = null;
    }

    // -------------------------------------------------------------------------
    // Session accessors
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if a user is currently authenticated.
     */
    public boolean isLoggedIn() {
        return currentAccount != null;
    }

    /**
     * Returns the currently authenticated account.
     *
     * @throws ATMException if no user is logged in
     */
    public Account getCurrentAccount() {
        if (currentAccount == null) {
            throw new ATMException(ErrorCode.INVALID_CREDENTIALS,
                    "No active session. Please log in.");
        }
        return currentAccount;
    }

    /**
     * Returns the currently authenticated user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Refreshes the in-memory current account from the database.
     * Called after any balance-changing operation.
     */
    public void refreshCurrentAccount() {
        if (currentAccount != null) {
            accountDAO.findByAccountNumber(currentAccount.getAccountNumber())
                      .ifPresent(a -> this.currentAccount = a);
        }
    }
}

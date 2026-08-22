package com.oasisinfobyte.atm.dao;

import com.oasisinfobyte.atm.database.DatabaseConnection;
import com.oasisinfobyte.atm.exception.DatabaseException;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.Account.AccountStatus;
import com.oasisinfobyte.atm.model.Account.AccountType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link Account} entities.
 *
 * <p>All SQL operations use {@link PreparedStatement} to prevent SQL injection.
 * Connections are managed via try-with-resources to prevent resource leaks.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class AccountDAO {

    private static final Logger LOGGER = Logger.getLogger(AccountDAO.class.getName());

    // -------------------------------------------------------------------------
    // SQL Statements
    // -------------------------------------------------------------------------

    private static final String SQL_FIND_BY_NUMBER =
            "SELECT account_number, user_id, pin_hash, balance, account_type, status, " +
            "failed_attempts, last_login, created_at, updated_at " +
            "FROM accounts WHERE account_number = ?";

    private static final String SQL_UPDATE_BALANCE =
            "UPDATE accounts SET balance = ?, updated_at = NOW() WHERE account_number = ?";

    private static final String SQL_UPDATE_PIN =
            "UPDATE accounts SET pin_hash = ?, updated_at = NOW() WHERE account_number = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE accounts SET status = ?, updated_at = NOW() WHERE account_number = ?";

    private static final String SQL_INCREMENT_FAILED =
            "UPDATE accounts SET failed_attempts = failed_attempts + 1, updated_at = NOW() " +
            "WHERE account_number = ?";

    private static final String SQL_RESET_FAILED =
            "UPDATE accounts SET failed_attempts = 0, last_login = NOW(), updated_at = NOW() " +
            "WHERE account_number = ?";

    private static final String SQL_INSERT =
            "INSERT INTO accounts (account_number, user_id, pin_hash, balance, account_type, status) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber the 16-digit account number
     * @return an {@link Optional} containing the account, or empty if not found
     */
    public Optional<Account> findByAccountNumber(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NUMBER)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding account: " + accountNumber, e);
            throw new DatabaseException("Error finding account", e);
        }
        return Optional.empty();
    }

    /**
     * Updates the balance of an account within an existing transaction.
     *
     * @param accountNumber the account to update
     * @param newBalance    the new balance to set
     * @param conn          the shared JDBC connection (for transaction atomicity)
     */
    public void updateBalance(String accountNumber, BigDecimal newBalance, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_BALANCE)) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating balance for account: " + accountNumber, e);
            throw new DatabaseException("Error updating account balance", e);
        }
    }

    /**
     * Updates the PIN hash for an account.
     *
     * @param accountNumber the account to update
     * @param newPinHash    the new BCrypt hash
     */
    public void updatePin(String accountNumber, String newPinHash) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PIN)) {

            ps.setString(1, newPinHash);
            ps.setString(2, accountNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating PIN for account: " + accountNumber, e);
            throw new DatabaseException("Error updating PIN", e);
        }
    }

    /**
     * Updates the status of an account (e.g., BLOCKED, ACTIVE).
     *
     * @param accountNumber the account to update
     * @param status        the new status
     */
    public void updateStatus(String accountNumber, AccountStatus status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            ps.setString(1, status.name());
            ps.setString(2, accountNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating status for account: " + accountNumber, e);
            throw new DatabaseException("Error updating account status", e);
        }
    }

    /**
     * Increments the failed login attempt counter for an account.
     *
     * @param accountNumber the account to update
     */
    public void incrementFailedAttempts(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INCREMENT_FAILED)) {

            ps.setString(1, accountNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error incrementing failed attempts", e);
            throw new DatabaseException("Error incrementing failed attempts", e);
        }
    }

    /**
     * Resets the failed login counter and records the login timestamp.
     *
     * @param accountNumber the account to update
     */
    public void resetFailedAttempts(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_RESET_FAILED)) {

            ps.setString(1, accountNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error resetting failed attempts", e);
            throw new DatabaseException("Error resetting failed attempts", e);
        }
    }

    /**
     * Saves a new account to the database.
     *
     * @param account the account to persist
     */
    public void save(Account account) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getUserId());
            ps.setString(3, account.getPinHash());
            ps.setBigDecimal(4, account.getBalance());
            ps.setString(5, account.getAccountType().name());
            ps.setString(6, account.getStatus().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving account", e);
            throw new DatabaseException("Error saving account: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountNumber(rs.getString("account_number"));
        account.setUserId(rs.getInt("user_id"));
        account.setPinHash(rs.getString("pin_hash"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        account.setStatus(AccountStatus.valueOf(rs.getString("status")));
        account.setFailedAttempts(rs.getInt("failed_attempts"));

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) account.setLastLogin(lastLogin.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) account.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) account.setUpdatedAt(updatedAt.toLocalDateTime());

        return account;
    }
}

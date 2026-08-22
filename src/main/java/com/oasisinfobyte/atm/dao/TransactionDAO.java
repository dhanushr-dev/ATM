package com.oasisinfobyte.atm.dao;

import com.oasisinfobyte.atm.database.DatabaseConnection;
import com.oasisinfobyte.atm.exception.DatabaseException;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.model.Transaction.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link Transaction} entities.
 *
 * <p>All SQL uses {@link PreparedStatement}. Inserts accept an external
 * {@link Connection} so they can participate in multi-statement transactions.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class TransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());

    // -------------------------------------------------------------------------
    // SQL Statements
    // -------------------------------------------------------------------------

    private static final String SQL_INSERT =
            "INSERT INTO transactions (account_number, transaction_type, amount, " +
            "balance_after, description, reference_number, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, NOW())";

    private static final String SQL_FIND_BY_ACCOUNT =
            "SELECT transaction_id, account_number, transaction_type, amount, " +
            "balance_after, description, reference_number, created_at " +
            "FROM transactions WHERE account_number = ? " +
            "ORDER BY created_at DESC, transaction_id DESC";

    private static final String SQL_FIND_MINI =
            "SELECT transaction_id, account_number, transaction_type, amount, " +
            "balance_after, description, reference_number, created_at " +
            "FROM transactions WHERE account_number = ? " +
            "ORDER BY created_at DESC, transaction_id DESC LIMIT ?";

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Persists a transaction record within an existing JDBC transaction.
     *
     * @param transaction the transaction to save
     * @param conn        the shared JDBC connection
     */
    public void save(Transaction transaction, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, transaction.getAccountNumber());
            ps.setString(2, transaction.getTransactionType().name());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setBigDecimal(4, transaction.getBalanceAfter());
            ps.setString(5, transaction.getDescription());
            ps.setString(6, transaction.getReferenceNumber());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setTransactionId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving transaction", e);
            throw new DatabaseException("Error saving transaction", e);
        }
    }

    /**
     * Returns all transactions for a given account number, newest first.
     *
     * @param accountNumber the account to query
     * @return list of all transactions
     */
    public List<Transaction> findByAccountNumber(String accountNumber) {
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ACCOUNT)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving transactions for: " + accountNumber, e);
            throw new DatabaseException("Error retrieving transaction history", e);
        }
        return list;
    }

    /**
     * Returns the most recent {@code limit} transactions for a given account.
     *
     * @param accountNumber the account to query
     * @param limit         maximum number of records to return
     * @return list of recent transactions
     */
    public List<Transaction> findRecentByAccountNumber(String accountNumber, int limit) {
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_MINI)) {

            ps.setString(1, accountNumber);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving mini statement for: " + accountNumber, e);
            throw new DatabaseException("Error retrieving mini statement", e);
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction txn = new Transaction();
        txn.setTransactionId(rs.getLong("transaction_id"));
        txn.setAccountNumber(rs.getString("account_number"));
        txn.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
        txn.setAmount(rs.getBigDecimal("amount"));
        txn.setBalanceAfter(rs.getBigDecimal("balance_after"));
        txn.setDescription(rs.getString("description"));
        txn.setReferenceNumber(rs.getString("reference_number"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) txn.setCreatedAt(createdAt.toLocalDateTime());

        return txn;
    }
}

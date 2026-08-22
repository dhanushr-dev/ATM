package com.oasisinfobyte.atm.dao;

import com.oasisinfobyte.atm.database.DatabaseConnection;
import com.oasisinfobyte.atm.exception.DatabaseException;
import com.oasisinfobyte.atm.model.User;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for {@link User} entities.
 *
 * <p>All SQL operations use {@link PreparedStatement} to prevent SQL injection.
 * Connections are managed via try-with-resources to prevent leaks.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    // -------------------------------------------------------------------------
    // SQL Statements
    // -------------------------------------------------------------------------

    private static final String SQL_FIND_BY_ID =
            "SELECT user_id, full_name, email, phone, created_at, updated_at " +
            "FROM users WHERE user_id = ?";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT user_id, full_name, email, phone, created_at, updated_at " +
            "FROM users WHERE email = ?";

    private static final String SQL_INSERT =
            "INSERT INTO users (full_name, email, phone) VALUES (?, ?, ?)";

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Finds a user by their primary key.
     *
     * @param userId the user's ID
     * @return an {@link Optional} containing the user, or empty if not found
     */
    public Optional<User> findById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: " + userId, e);
            throw new DatabaseException("Error finding user by ID", e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by email address.
     *
     * @param email the user's email
     * @return an {@link Optional} containing the user, or empty if not found
     */
    public Optional<User> findByEmail(String email) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by email", e);
            throw new DatabaseException("Error finding user by email", e);
        }
        return Optional.empty();
    }

    /**
     * Saves a new user to the database.
     *
     * @param user the user to save (userId will be populated after insert)
     * @return the saved user with generated ID
     */
    public User save(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Saving user failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
            return user;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving user", e);
            throw new DatabaseException("Error saving user: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) user.setUpdatedAt(updatedAt.toLocalDateTime());

        return user;
    }
}

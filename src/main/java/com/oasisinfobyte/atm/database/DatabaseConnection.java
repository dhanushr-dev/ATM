package com.oasisinfobyte.atm.database;

import com.oasisinfobyte.atm.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager for the ATM Interface.
 *
 * <p>Loads connection parameters from {@code database.properties} on the
 * classpath and provides a simple {@link #getConnection()} method used
 * throughout the DAO layer.</p>
 *
 * <p>Usage (try-with-resources pattern):</p>
 * <pre>{@code
 * try (Connection conn = DatabaseConnection.getConnection()) {
 *     // use connection
 * }
 * }</pre>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String PROPS_FILE = "database.properties";

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    /** Eager-load properties at class initialisation. */
    static {
        loadProperties();
    }

    /** Private constructor — utility class, never instantiated. */
    private DatabaseConnection() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Opens and returns a new JDBC {@link Connection}.
     *
     * <p>The caller is responsible for closing the connection (best done via
     * try-with-resources).</p>
     *
     * @return an open {@link Connection}
     * @throws DatabaseException if the connection cannot be established
     */
    public static Connection getConnection() {
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC driver not found: " + driver, e);
            throw new DatabaseException("JDBC driver not found: " + driver, e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Cannot connect to database", e);
            throw new DatabaseException("Cannot connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Tests whether a connection to the database can be established.
     *
     * @return {@code true} if the connection succeeds
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream(PROPS_FILE)) {

            if (is == null) {
                throw new DatabaseException("Cannot find " + PROPS_FILE + " on the classpath");
            }
            props.load(is);

            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            driver   = props.getProperty("db.driver");

            LOGGER.info("Database properties loaded successfully.");

        } catch (IOException e) {
            throw new DatabaseException("Failed to load " + PROPS_FILE, e);
        }
    }
}

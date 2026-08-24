package com.oasisinfobyte.atm.database;

import com.oasisinfobyte.atm.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connectivity for the ATM Interface.
 *
 * <p>Includes automatic failover to an Embedded H2 database engine
 * if MySQL is unreachable in cloud environments like Render.</p>
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String PROPS_FILE = "application.properties";

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    private static boolean useH2Fallback = false;
    private static boolean h2Initialized = false;

    static {
        loadProperties();
    }

    private DatabaseConnection() {}

    public static Connection getConnection() {
        if (useH2Fallback) {
            return getH2Connection();
        }
        try {
            Class.forName(driver);
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "MySQL connection failed ({0}). Switching to Embedded H2 Fallback Database!", e.getMessage());
            useH2Fallback = true;
            return getH2Connection();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "MySQL Driver not found. Switching to Embedded H2 Fallback Database!");
            useH2Fallback = true;
            return getH2Connection();
        }
    }

    private static synchronized Connection getH2Connection() {
        try {
            Class.forName("org.h2.Driver");
            String h2Url = "jdbc:h2:mem:atm_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";
            Connection conn = DriverManager.getConnection(h2Url, "sa", "");
            if (!h2Initialized) {
                h2Initialized = true;
                initH2Database(conn);
            }
            return conn;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to H2 Fallback Database", e);
            throw new DatabaseException("Database connection failed: " + e.getMessage(), e);
        }
    }

    private static void initH2Database(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            LOGGER.info("Initializing Embedded H2 Database schema and sample data...");
            InputStream schemaStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("sql/schema.sql");
            if (schemaStream != null) {
                String schemaSql = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
                schemaSql = schemaSql.replaceAll("(?i)CREATE DATABASE IF NOT EXISTS.*?;", "")
                                     .replaceAll("(?i)USE atm_db;", "");
                for (String query : schemaSql.split(";")) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty()) {
                        try { stmt.execute(trimmed); } catch (Exception ignored) {}
                    }
                }
            }
            InputStream sampleStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("sql/sample_data.sql");
            if (sampleStream != null) {
                String sampleSql = new String(sampleStream.readAllBytes(), StandardCharsets.UTF_8);
                sampleSql = sampleSql.replaceAll("(?i)USE atm_db;", "");
                for (String query : sampleSql.split(";")) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty()) {
                        try { stmt.execute(trimmed); } catch (Exception ignored) {}
                    }
                }
            }
            LOGGER.info("🎉 Embedded H2 Database initialized successfully!");
        } catch (Exception e) {
            LOGGER.warning("Error initializing H2 schema: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(PROPS_FILE)) {
            if (is != null) {
                props.load(is);
            }

            String envHost = System.getenv("MYSQLHOST");
            String envPort = System.getenv("MYSQLPORT");
            String envDb   = System.getenv("MYSQLDATABASE");
            String envUser = System.getenv("MYSQLUSER");
            String envPass = System.getenv("MYSQLPASSWORD");
            String envUrl  = System.getenv("DB_URL");

            if (envHost != null && !envHost.isBlank()) {
                url = "jdbc:mysql://" + envHost + ":" + (envPort != null ? envPort : "3306") + "/" + (envDb != null ? envDb : "railway") + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                username = envUser != null ? envUser : "root";
                password = envPass != null ? envPass : "tbYrETJfetbesRhyaaQfgJjgCMMFsVhV";
            } else if (envUrl != null && !envUrl.isBlank()) {
                url = envUrl.replace("mysql-production-2b57.up.railway.app", "mysql.railway.internal");
                username = System.getenv("DB_USERNAME");
                password = System.getenv("DB_PASSWORD");
            } else if (java.awt.GraphicsEnvironment.isHeadless()) {
                url = "jdbc:mysql://localhost:3306/atm_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                username = "root";
                password = "password";
            } else {
                url      = props.getProperty("db.url");
                username = props.getProperty("db.username");
                password = props.getProperty("db.password");
            }
            driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

            LOGGER.info("Database properties loaded successfully.");
        } catch (IOException e) {
            throw new DatabaseException("Failed to load " + PROPS_FILE, e);
        }
    }
}

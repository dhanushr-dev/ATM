package com.oasisinfobyte.atm.tools;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Logger;

public class InitRailwayDatabase {

    private static final Logger LOGGER = Logger.getLogger(InitRailwayDatabase.class.getName());

    public static void main(String[] args) {
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");
        String db   = System.getenv("MYSQLDATABASE");

        if (host == null || host.isBlank()) host = System.getenv("DB_HOST");
        if (host == null || host.isBlank()) host = "mysql.railway.internal";
        if (port == null || port.isBlank()) port = "3306";
        if (user == null || user.isBlank()) user = "root";
        if (pass == null || pass.isBlank()) pass = "tbYrETJfetbesRhyaaQfgJjgCMMFsVhV";
        if (db   == null || db.isBlank())   db   = "railway";

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        LOGGER.info("Connecting to Railway MySQL at: " + host + ":" + port + "/" + db);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
             Statement stmt = conn.createStatement()) {

            LOGGER.info("Connected successfully to Railway MySQL!");

            // 1. Run schema.sql
            InputStream schemaStream = InitRailwayDatabase.class.getClassLoader().getResourceAsStream("sql/schema.sql");
            if (schemaStream != null) {
                String schemaSql = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
                // Remove USE statement so it executes inside current database context
                schemaSql = schemaSql.replaceAll("(?i)CREATE DATABASE IF NOT EXISTS.*?;", "")
                                     .replaceAll("(?i)USE atm_db;", "");
                for (String query : schemaSql.split(";")) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (Exception e) {
                            LOGGER.warning("Schema query error (ignored if exists): " + e.getMessage());
                        }
                    }
                }
                LOGGER.info("Schema loaded successfully into Railway database!");
            }

            // 2. Run sample_data.sql
            InputStream sampleStream = InitRailwayDatabase.class.getClassLoader().getResourceAsStream("sql/sample_data.sql");
            if (sampleStream != null) {
                String sampleSql = new String(sampleStream.readAllBytes(), StandardCharsets.UTF_8);
                sampleSql = sampleSql.replaceAll("(?i)USE atm_db;", "");
                for (String query : sampleSql.split(";")) {
                    String trimmed = query.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (Exception e) {
                            LOGGER.warning("Sample data query error: " + e.getMessage());
                        }
                    }
                }
                LOGGER.info("Sample data loaded successfully into Railway database!");
            }

            LOGGER.info("🎉 Railway MySQL Database initialization complete!");

        } catch (Exception e) {
            LOGGER.severe("Error initializing Railway database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

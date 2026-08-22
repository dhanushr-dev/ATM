package com.oasisinfobyte.atm;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.TransactionDAO;
import com.oasisinfobyte.atm.dao.UserDAO;
import com.oasisinfobyte.atm.database.DatabaseConnection;
import com.oasisinfobyte.atm.service.AccountService;
import com.oasisinfobyte.atm.service.AuthService;
import com.oasisinfobyte.atm.ui.LoginFrame;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point for the ATM Interface.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // ── 1. Enable anti-aliased text system-wide ──
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // ── 2. Use cross-platform L&F so our dark colours are respected ──
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set L&F", e);
        }

        // ── 3. Apply global theme defaults after L&F ──
        ATMTheme.applyGlobalDefaults();

        // ── 4. Check database & Auto-initialize if running in headless cloud (e.g. Railway) ──
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            LOGGER.info("Headless environment detected (Railway / Cloud deployment).");
            try {
                com.oasisinfobyte.atm.tools.InitRailwayDatabase.main(args);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Cloud database auto-init notice: " + e.getMessage());
            }

            // Start lightweight HTTP server for Railway health check on port 8080
            try {
                int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
                com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);
                server.createContext("/", exchange -> {
                    String response = "<html><body style='font-family:sans-serif;background:#081024;color:#00C6FF;padding:40px;'>"
                            + "<h1>🏦 ATM Interface Cloud Service is Running!</h1>"
                            + "<p style='color:#e6f2ff;'>Database Status: <b>Connected & Initialized</b></p>"
                            + "<p style='color:#82a5d2;'>GitHub Repo: <a style='color:#00C6FF;' href='https://github.com/dhanushr-dev/ATM'>dhanushr-dev/ATM</a></p>"
                            + "</body></html>";
                    byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, bytes.length);
                    java.io.OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                });
                server.start();
                LOGGER.info("Cloud Health Check HTTP Server started on port " + port);
                return;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start HTTP server", e);
            }
        }

        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Cannot connect to the database.</b><br><br>" +
                    "Please ensure:<br>" +
                    "• MySQL is running on localhost:3306<br>" +
                    "• Database 'atm_db' exists (run schema.sql)<br>" +
                    "• Credentials in database.properties are correct</html>",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ── 5. Build dependency graph ──
        UserDAO        userDAO        = new UserDAO();
        AccountDAO     accountDAO     = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        AuthService    authService    = new AuthService(accountDAO, userDAO);
        AccountService accountService = new AccountService(accountDAO, transactionDAO, authService);
        ATMController  controller     = new ATMController(authService, accountService);

        // ── 6. Launch on EDT ──
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame(controller);
            login.setVisible(true);
            LOGGER.info("ATM Interface v2.0 started.");
        });
    }
}

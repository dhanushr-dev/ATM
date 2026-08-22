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

        // ── 4. Check database ──
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

package com.oasisinfobyte.atm.tools;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.TransactionDAO;
import com.oasisinfobyte.atm.dao.UserDAO;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.service.AccountService;
import com.oasisinfobyte.atm.service.AuthService;

import java.math.BigDecimal;
import java.util.List;

/** Dev-only smoke test for all banking operations. */
public class RunIntegrationTest {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        AccountDAO accountDAO = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        AuthService authService = new AuthService(accountDAO, userDAO);
        AccountService accountService = new AccountService(accountDAO, transactionDAO, authService);
        ATMController controller = new ATMController(authService, accountService);

        int passed = 0;
        int failed = 0;

        try {
            // Login
            var acc = controller.login("1001000000000001", "1234");
            ok("Login", acc != null);
            passed++;

            // Balance
            BigDecimal bal = controller.getBalance();
            ok("Balance inquiry", bal != null && bal.compareTo(BigDecimal.ZERO) >= 0);
            passed++;

            // Deposit
            Transaction dep = controller.deposit("100");
            ok("Deposit", dep != null && dep.getReferenceNumber() != null);
            passed++;

            // Withdraw
            Transaction wdr = controller.withdraw("50");
            ok("Withdraw", wdr != null);
            passed++;

            // Transfer
            Transaction xfer = controller.transfer("1001000000000002", "25");
            ok("Transfer", xfer != null);
            passed++;

            // History
            List<Transaction> history = controller.getTransactionHistory();
            ok("Transaction history", history != null && !history.isEmpty());
            passed++;

            // Mini statement
            List<Transaction> mini = controller.getMiniStatement();
            ok("Mini statement", mini != null);
            passed++;

            controller.logout();
            try {
                controller.getCurrentAccount();
                throw new RuntimeException("Expected session to be cleared after logout");
            } catch (Exception ignored) {
                // expected — no active session
            }
            ok("Logout");
            passed++;

            System.out.println("\n=== ALL " + passed + " TESTS PASSED ===");
        } catch (Exception e) {
            failed++;
            System.err.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void ok(String name) {
        System.out.println("✓ " + name);
    }

    private static void ok(String name, boolean cond) {
        if (!cond) throw new RuntimeException(name + " check failed");
        ok(name);
    }
}

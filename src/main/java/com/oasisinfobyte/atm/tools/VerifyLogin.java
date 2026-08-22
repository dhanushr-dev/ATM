package com.oasisinfobyte.atm.tools;

import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.UserDAO;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.service.AuthService;

public class VerifyLogin {
    public static void main(String[] args) {
        AuthService auth = new AuthService(new AccountDAO(), new UserDAO());
        String[][] tests = {
            {"1001000000000001", "1234"},
            {"1001000000000002", "5678"},
            {"1001000000000003", "9999"},
        };
        for (String[] t : tests) {
            try {
                Account acc = auth.login(t[0], t[1]);
                System.out.println("✓ Login OK: " + t[0] + " | Balance: " + acc.getBalance());
                auth.logout();
            } catch (Exception e) {
                System.out.println("✗ Login FAILED: " + t[0] + " -> " + e.getMessage());
            }
        }
    }
}

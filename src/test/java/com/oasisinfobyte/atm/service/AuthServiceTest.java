package com.oasisinfobyte.atm.service;

import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.UserDAO;
import com.oasisinfobyte.atm.exception.ATMException;
import com.oasisinfobyte.atm.exception.AccountNotFoundException;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.utility.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService} testing authentication and registration logic.
 */
class AuthServiceTest {

    private AccountDAO accountDAO;
    private UserDAO    userDAO;
    private AuthService authService;

    private Account testAccount;
    private User    testUser;

    @BeforeEach
    void setUp() {
        accountDAO  = mock(AccountDAO.class);
        userDAO     = mock(UserDAO.class);
        authService = new AuthService(accountDAO, userDAO);

        testUser = new User();
        testUser.setUserId(1);
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setAccountNumber("1001000000000001");
        testAccount.setUserId(1);
        testAccount.setPinHash(PasswordUtil.hashPin("1234"));
        testAccount.setBalance(new BigDecimal("25000.00"));
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
        testAccount.setFailedAttempts(0);
    }

    @Test
    void login_validCredentials_returnsAccount() {
        when(accountDAO.findByAccountNumber("1001000000000001")).thenReturn(Optional.of(testAccount));
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        Account acc = authService.login("1001000000000001", "1234");
        assertNotNull(acc);
        assertEquals("1001000000000001", acc.getAccountNumber());
        assertTrue(authService.isLoggedIn());
        assertEquals("Test User", authService.getCurrentUser().getFullName());
    }

    @Test
    void login_accountNotFound_throwsAccountNotFoundException() {
        when(accountDAO.findByAccountNumber("9999999999999999")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> authService.login("9999999999999999", "1234"));
    }

    @Test
    void login_wrongPin_incrementsFailedAttemptsAndThrows() {
        when(accountDAO.findByAccountNumber("1001000000000001")).thenReturn(Optional.of(testAccount));

        assertThrows(ATMException.class, () -> authService.login("1001000000000001", "0000"));
        verify(accountDAO, times(1)).incrementFailedAttempts("1001000000000001");
    }

    @Test
    void login_blockedAccount_throwsATMException() {
        testAccount.setStatus(Account.AccountStatus.BLOCKED);
        when(accountDAO.findByAccountNumber("1001000000000001")).thenReturn(Optional.of(testAccount));

        ATMException ex = assertThrows(ATMException.class, () -> authService.login("1001000000000001", "1234"));
        assertTrue(ex.getMessage().contains("blocked"));
    }

    @Test
    void logout_clearsSession() {
        when(accountDAO.findByAccountNumber("1001000000000001")).thenReturn(Optional.of(testAccount));
        when(userDAO.findById(1)).thenReturn(Optional.of(testUser));

        authService.login("1001000000000001", "1234");
        assertTrue(authService.isLoggedIn());

        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertThrows(ATMException.class, () -> authService.getCurrentAccount());
    }

    @Test
    void registerAccount_validData_returnsNewAccount() {
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(99);
            return u;
        });

        Account newAcc = authService.registerAccount(
                "Jane Doe", "jane@example.com", "9876543210",
                "4321", new BigDecimal("1000.00"), Account.AccountType.SAVINGS);

        assertNotNull(newAcc);
        assertTrue(newAcc.getAccountNumber().startsWith("1001"));
        assertEquals(new BigDecimal("1000.00"), newAcc.getBalance());
        assertEquals(Account.AccountStatus.ACTIVE, newAcc.getStatus());
        verify(accountDAO, times(1)).save(any(Account.class));
    }

    @Test
    void registerAccount_invalidPhone_throwsATMException() {
        assertThrows(ATMException.class, () -> authService.registerAccount(
                "Jane Doe", "jane@example.com", "123",
                "4321", new BigDecimal("1000.00"), Account.AccountType.SAVINGS));
    }

    @Test
    void registerAccount_initialDepositTooLow_throwsATMException() {
        assertThrows(ATMException.class, () -> authService.registerAccount(
                "Jane Doe", "jane@example.com", "9876543210",
                "4321", new BigDecimal("100.00"), Account.AccountType.SAVINGS));
    }
}

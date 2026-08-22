package com.oasisinfobyte.atm.service;

import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.TransactionDAO;
import com.oasisinfobyte.atm.exception.InsufficientFundsException;
import com.oasisinfobyte.atm.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AccountService} using Mockito mocks.
 *
 * <p>Database interactions are mocked so these tests run without a live DB.
 * Tests focus on business-logic validation that does not reach the database.</p>
 *
 * @author Oasis Infobyte ATM Project
 */
class AccountServiceTest {

    private AccountDAO     accountDAO;
    private TransactionDAO transactionDAO;
    private AuthService    authService;

    private AccountService accountService;
    private Account        mockAccount;

    @BeforeEach
    void setUp() {
        accountDAO     = mock(AccountDAO.class);
        transactionDAO = mock(TransactionDAO.class);
        authService    = mock(AuthService.class);

        accountService = new AccountService(accountDAO, transactionDAO, authService);

        mockAccount = new Account();
        mockAccount.setAccountNumber("1001000000000001");
        mockAccount.setBalance(new BigDecimal("10000.00"));
        mockAccount.setStatus(Account.AccountStatus.ACTIVE);

        when(authService.getCurrentAccount()).thenReturn(mockAccount);
    }

    // -------------------------------------------------------------------------
    // Withdrawal — validation fires before DB is touched
    // -------------------------------------------------------------------------

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException() {
        // 50000 > 10000 balance — InsufficientFundsException thrown before DB call
        assertThrows(InsufficientFundsException.class,
                () -> accountService.withdraw("50000"));
    }

    @Test
    void withdraw_invalidAmount_throwsATMException() {
        assertThrows(com.oasisinfobyte.atm.exception.ATMException.class,
                () -> accountService.withdraw("-100"));
    }

    @Test
    void withdraw_zeroAmount_throwsATMException() {
        assertThrows(com.oasisinfobyte.atm.exception.ATMException.class,
                () -> accountService.withdraw("0"));
    }

    @Test
    void withdraw_nonNumericAmount_throwsATMException() {
        assertThrows(com.oasisinfobyte.atm.exception.ATMException.class,
                () -> accountService.withdraw("abc"));
    }

    @Test
    void withdraw_exceedsTransactionLimit_throwsATMException() {
        // Max per transaction is 50000 — validation fires first
        assertThrows(com.oasisinfobyte.atm.exception.ATMException.class,
                () -> accountService.withdraw("60000"));
    }

    // -------------------------------------------------------------------------
    // Transfer — validation fires before DB is touched
    // -------------------------------------------------------------------------

    @Test
    void transfer_toSameAccount_throwsATMException() {
        assertThrows(com.oasisinfobyte.atm.exception.ATMException.class,
                () -> accountService.transfer("1001000000000001", "1000"));
    }

    @Test
    void transfer_destinationNotFound_throwsAccountNotFoundException() {
        when(accountDAO.findByAccountNumber("9999999999999999"))
                .thenReturn(Optional.empty());

        assertThrows(com.oasisinfobyte.atm.exception.AccountNotFoundException.class,
                () -> accountService.transfer("9999999999999999", "1000"));
    }

    @Test
    void transfer_insufficientFunds_throwsInsufficientFundsException() {
        Account dest = new Account();
        dest.setAccountNumber("9999999999999999");
        dest.setBalance(BigDecimal.ZERO);
        dest.setStatus(Account.AccountStatus.ACTIVE);
        when(accountDAO.findByAccountNumber("9999999999999999"))
                .thenReturn(Optional.of(dest));

        // 50000 > 10000 balance — InsufficientFundsException before DB write
        assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer("9999999999999999", "50000"));
    }

    @Test
    void transfer_invalidDestinationAccountNumber_throwsATMException() {
        assertThrows(com.oasisinfobyte.atm.exception.ATMException.class,
                () -> accountService.transfer("INVALID", "1000"));
    }
}

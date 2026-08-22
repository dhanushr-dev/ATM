package com.oasisinfobyte.atm.exception;

/**
 * Thrown when a requested account cannot be found in the database.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class AccountNotFoundException extends ATMException {

    private final String accountNumber;

    public AccountNotFoundException(String accountNumber) {
        super(ErrorCode.ACCOUNT_NOT_FOUND,
              "Account not found: " + accountNumber);
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() { return accountNumber; }
}

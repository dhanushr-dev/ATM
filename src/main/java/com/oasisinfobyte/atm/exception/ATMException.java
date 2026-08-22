package com.oasisinfobyte.atm.exception;

/**
 * Base application exception for the ATM Interface system.
 * All custom exceptions extend this class.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class ATMException extends RuntimeException {

    private final ErrorCode errorCode;

    public ATMException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ATMException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }

    // -------------------------------------------------------------------------
    // Error code catalog
    // -------------------------------------------------------------------------

    /**
     * Enumeration of all structured error codes used across the application.
     */
    public enum ErrorCode {
        // Authentication
        INVALID_CREDENTIALS,
        ACCOUNT_LOCKED,
        ACCOUNT_BLOCKED,
        ACCOUNT_NOT_FOUND,

        // Transactions
        INSUFFICIENT_FUNDS,
        INVALID_AMOUNT,
        TRANSFER_TO_SELF,
        DESTINATION_ACCOUNT_NOT_FOUND,
        DAILY_LIMIT_EXCEEDED,

        // PIN
        INVALID_PIN_FORMAT,
        PIN_MISMATCH,

        // Database
        DATABASE_ERROR,
        CONNECTION_ERROR,

        // General
        VALIDATION_ERROR,
        UNKNOWN_ERROR
    }
}

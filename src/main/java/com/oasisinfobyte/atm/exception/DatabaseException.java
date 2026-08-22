package com.oasisinfobyte.atm.exception;

/**
 * Thrown when a database operation fails unexpectedly.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class DatabaseException extends ATMException {

    public DatabaseException(String message, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, message, cause);
    }

    public DatabaseException(String message) {
        super(ErrorCode.DATABASE_ERROR, message);
    }
}

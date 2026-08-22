package com.oasisinfobyte.atm.validation;

import com.oasisinfobyte.atm.exception.ATMException;
import com.oasisinfobyte.atm.exception.ATMException.ErrorCode;

import java.math.BigDecimal;

/**
 * Centralised input validation for the ATM Interface.
 *
 * <p>All validation methods throw {@link ATMException} with a descriptive
 * message when a constraint is violated.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public final class InputValidator {

    // Limits
    public static final int    PIN_LENGTH        = 4;
    public static final BigDecimal MIN_AMOUNT    = new BigDecimal("1.00");
    public static final BigDecimal MAX_DEPOSIT   = new BigDecimal("100000.00");
    public static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("50000.00");
    public static final BigDecimal MAX_TRANSFER  = new BigDecimal("50000.00");

    /** Private constructor — utility class. */
    private InputValidator() {}

    // -------------------------------------------------------------------------
    // Account number
    // -------------------------------------------------------------------------

    /**
     * Validates that the account number is exactly 16 numeric digits.
     *
     * @throws ATMException if validation fails
     */
    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR,
                    "Account number must not be empty.");
        }
        if (!accountNumber.matches("\\d{16}")) {
            throw new ATMException(ErrorCode.VALIDATION_ERROR,
                    "Account number must be exactly 16 digits.");
        }
    }

    // -------------------------------------------------------------------------
    // PIN
    // -------------------------------------------------------------------------

    /**
     * Validates that the PIN is exactly 4 numeric digits.
     *
     * @throws ATMException if validation fails
     */
    public static void validatePin(String pin) {
        if (pin == null || pin.isBlank()) {
            throw new ATMException(ErrorCode.INVALID_PIN_FORMAT, "PIN must not be empty.");
        }
        if (!pin.matches("\\d{" + PIN_LENGTH + "}")) {
            throw new ATMException(ErrorCode.INVALID_PIN_FORMAT,
                    "PIN must be exactly " + PIN_LENGTH + " digits.");
        }
    }

    /**
     * Validates that two PIN entries match (used during PIN change).
     *
     * @throws ATMException if they do not match
     */
    public static void validatePinMatch(String pin, String confirmPin) {
        validatePin(pin);
        validatePin(confirmPin);
        if (!pin.equals(confirmPin)) {
            throw new ATMException(ErrorCode.PIN_MISMATCH, "PINs do not match.");
        }
    }

    // -------------------------------------------------------------------------
    // Amounts
    // -------------------------------------------------------------------------

    /**
     * Validates a deposit amount.
     *
     * @param amount string entered by the user
     * @return parsed {@link BigDecimal}
     * @throws ATMException if validation fails
     */
    public static BigDecimal validateDepositAmount(String amount) {
        BigDecimal parsed = parseAmount(amount);
        if (parsed.compareTo(MIN_AMOUNT) < 0) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Minimum deposit amount is ₹" + MIN_AMOUNT + ".");
        }
        if (parsed.compareTo(MAX_DEPOSIT) > 0) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Maximum single deposit is ₹" + MAX_DEPOSIT + ".");
        }
        return parsed;
    }

    /**
     * Validates a withdrawal amount.
     *
     * @param amount string entered by the user
     * @return parsed {@link BigDecimal}
     * @throws ATMException if validation fails
     */
    public static BigDecimal validateWithdrawalAmount(String amount) {
        BigDecimal parsed = parseAmount(amount);
        if (parsed.compareTo(MIN_AMOUNT) < 0) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Minimum withdrawal amount is ₹" + MIN_AMOUNT + ".");
        }
        if (parsed.compareTo(MAX_WITHDRAWAL) > 0) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Maximum single withdrawal is ₹" + MAX_WITHDRAWAL + ".");
        }
        return parsed;
    }

    /**
     * Validates a transfer amount.
     *
     * @param amount string entered by the user
     * @return parsed {@link BigDecimal}
     * @throws ATMException if validation fails
     */
    public static BigDecimal validateTransferAmount(String amount) {
        BigDecimal parsed = parseAmount(amount);
        if (parsed.compareTo(MIN_AMOUNT) < 0) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Minimum transfer amount is ₹" + MIN_AMOUNT + ".");
        }
        if (parsed.compareTo(MAX_TRANSFER) > 0) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Maximum single transfer is ₹" + MAX_TRANSFER + ".");
        }
        return parsed;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static BigDecimal parseAmount(String amount) {
        if (amount == null || amount.isBlank()) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT, "Amount must not be empty.");
        }
        try {
            BigDecimal value = new BigDecimal(amount.trim());
            if (value.scale() > 2) {
                throw new ATMException(ErrorCode.INVALID_AMOUNT,
                        "Amount cannot have more than 2 decimal places.");
            }
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ATMException(ErrorCode.INVALID_AMOUNT,
                        "Amount must be greater than zero.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ATMException(ErrorCode.INVALID_AMOUNT,
                    "Invalid amount format. Please enter a valid number (e.g. 500 or 1500.50).");
        }
    }
}

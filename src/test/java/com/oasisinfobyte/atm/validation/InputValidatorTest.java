package com.oasisinfobyte.atm.validation;

import com.oasisinfobyte.atm.exception.ATMException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InputValidator}.
 *
 * @author Oasis Infobyte ATM Project
 */
class InputValidatorTest {

    // -------------------------------------------------------------------------
    // Account number
    // -------------------------------------------------------------------------

    @Test
    void validateAccountNumber_validInput_noException() {
        assertDoesNotThrow(() -> InputValidator.validateAccountNumber("1001000000000001"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "123", "ABCD1234EFGH5678", "123456789012345A"})
    void validateAccountNumber_invalidInput_throwsException(String input) {
        assertThrows(ATMException.class, () -> InputValidator.validateAccountNumber(input));
    }

    // -------------------------------------------------------------------------
    // PIN
    // -------------------------------------------------------------------------

    @Test
    void validatePin_validFourDigit_noException() {
        assertDoesNotThrow(() -> InputValidator.validatePin("1234"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12", "12345", "abcd", "12 4"})
    void validatePin_invalidInput_throwsException(String input) {
        assertThrows(ATMException.class, () -> InputValidator.validatePin(input));
    }

    // -------------------------------------------------------------------------
    // PIN match
    // -------------------------------------------------------------------------

    @Test
    void validatePinMatch_matching_noException() {
        assertDoesNotThrow(() -> InputValidator.validatePinMatch("1234", "1234"));
    }

    @Test
    void validatePinMatch_notMatching_throwsException() {
        ATMException ex = assertThrows(ATMException.class,
                () -> InputValidator.validatePinMatch("1234", "5678"));
        assertEquals(ATMException.ErrorCode.PIN_MISMATCH, ex.getErrorCode());
    }

    // -------------------------------------------------------------------------
    // Deposit amount
    // -------------------------------------------------------------------------

    @Test
    void validateDepositAmount_valid_returnsParsed() {
        BigDecimal result = InputValidator.validateDepositAmount("5000");
        assertEquals(new BigDecimal("5000"), result);
    }

    @Test
    void validateDepositAmount_zero_throwsException() {
        assertThrows(ATMException.class, () -> InputValidator.validateDepositAmount("0"));
    }

    @Test
    void validateDepositAmount_exceedsMax_throwsException() {
        assertThrows(ATMException.class, () -> InputValidator.validateDepositAmount("200000"));
    }

    @Test
    void validateDepositAmount_negativeValue_throwsException() {
        assertThrows(ATMException.class, () -> InputValidator.validateDepositAmount("-100"));
    }

    @Test
    void validateDepositAmount_nonNumeric_throwsException() {
        assertThrows(ATMException.class, () -> InputValidator.validateDepositAmount("abc"));
    }

    // -------------------------------------------------------------------------
    // Withdrawal amount
    // -------------------------------------------------------------------------

    @Test
    void validateWithdrawalAmount_valid_returnsParsed() {
        BigDecimal result = InputValidator.validateWithdrawalAmount("1000");
        assertEquals(new BigDecimal("1000"), result);
    }

    @Test
    void validateWithdrawalAmount_exceedsMax_throwsException() {
        assertThrows(ATMException.class, () -> InputValidator.validateWithdrawalAmount("60000"));
    }

    // -------------------------------------------------------------------------
    // Transfer amount
    // -------------------------------------------------------------------------

    @Test
    void validateTransferAmount_valid_returnsParsed() {
        BigDecimal result = InputValidator.validateTransferAmount("2500.50");
        assertEquals(new BigDecimal("2500.50"), result);
    }

    @Test
    void validateTransferAmount_tooManyDecimals_throwsException() {
        assertThrows(ATMException.class, () -> InputValidator.validateTransferAmount("100.123"));
    }
}

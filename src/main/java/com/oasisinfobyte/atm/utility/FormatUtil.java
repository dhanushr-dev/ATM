package com.oasisinfobyte.atm.utility;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for consistent number and string formatting across the UI.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.1.0
 */
public final class FormatUtil {

    // Fixed: use Locale.forLanguageTag to avoid deprecated Locale(String,String) constructor
    private static final Locale INDIA_LOCALE = Locale.forLanguageTag("en-IN");

    /** Private constructor — utility class. */
    private FormatUtil() {}

    /**
     * Formats a {@link BigDecimal} as Indian Rupee currency.
     * Creates a fresh NumberFormat per call (thread-safe).
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        // NumberFormat.getCurrencyInstance is not thread-safe; create per-call
        NumberFormat fmt = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        return fmt.format(amount);
    }

    /**
     * Masks an account number showing only the last 4 digits.
     * e.g. {@code 1001000000000001} → {@code ************0001}
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return "****";
        return "•".repeat(accountNumber.length() - 4)
               + accountNumber.substring(accountNumber.length() - 4);
    }

    /**
     * Formats a plain number with Indian comma grouping.
     * e.g. {@code 125000.50} → {@code 1,25,000.50}
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    /** Centers a string within a fixed width (for monospace receipt layout). */
    public static String center(String text, int width) {
        if (text == null || text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }
}

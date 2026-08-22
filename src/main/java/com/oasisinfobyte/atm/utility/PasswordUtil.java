package com.oasisinfobyte.atm.utility;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for PIN hashing and verification using BCrypt.
 *
 * <p>BCrypt is intentionally slow and resistant to brute-force attacks,
 * making it ideal for securing ATM PINs stored in the database.</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public final class PasswordUtil {

    /** BCrypt work factor (cost). Higher = slower hashing = better security. */
    private static final int BCRYPT_COST = 12;

    /** Private constructor — utility class, never instantiated. */
    private PasswordUtil() {}

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Hashes a plain-text PIN using BCrypt.
     *
     * @param plainPin the raw PIN entered by the user
     * @return BCrypt hash string suitable for database storage
     */
    public static String hashPin(String plainPin) {
        if (plainPin == null || plainPin.isBlank()) {
            throw new IllegalArgumentException("PIN must not be null or blank");
        }
        return BCrypt.hashpw(plainPin, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifies a plain-text PIN against a stored BCrypt hash.
     *
     * @param plainPin   the raw PIN entered by the user
     * @param hashedPin  the stored BCrypt hash from the database
     * @return {@code true} if the PIN matches the hash
     */
    public static boolean verifyPin(String plainPin, String hashedPin) {
        if (plainPin == null || hashedPin == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPin, hashedPin);
        } catch (IllegalArgumentException e) {
            // Invalid hash format stored in DB — treat as mismatch
            return false;
        }
    }
}

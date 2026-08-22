package com.oasisinfobyte.atm.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PasswordUtil}.
 *
 * @author Oasis Infobyte ATM Project
 */
class PasswordUtilTest {

    @Test
    void hashPin_returnsNonNullBCryptHash() {
        String hash = PasswordUtil.hashPin("1234");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    void hashPin_samePin_producesDifferentHashes() {
        // BCrypt uses random salt — same input must produce different outputs
        String hash1 = PasswordUtil.hashPin("1234");
        String hash2 = PasswordUtil.hashPin("1234");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void verifyPin_correctPin_returnsTrue() {
        String hash = PasswordUtil.hashPin("5678");
        assertTrue(PasswordUtil.verifyPin("5678", hash));
    }

    @Test
    void verifyPin_wrongPin_returnsFalse() {
        String hash = PasswordUtil.hashPin("5678");
        assertFalse(PasswordUtil.verifyPin("1234", hash));
    }

    @Test
    void verifyPin_nullPin_returnsFalse() {
        String hash = PasswordUtil.hashPin("1234");
        assertFalse(PasswordUtil.verifyPin(null, hash));
    }

    @Test
    void verifyPin_nullHash_returnsFalse() {
        assertFalse(PasswordUtil.verifyPin("1234", null));
    }

    @Test
    void hashPin_nullPin_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPin(null));
    }

    @Test
    void hashPin_blankPin_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPin("  "));
    }
}

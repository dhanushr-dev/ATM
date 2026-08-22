package com.oasisinfobyte.atm.tools;

import org.mindrot.jbcrypt.BCrypt;

/**
 * One-time utility to generate correct BCrypt hashes for sample data PINs.
 * Run this class directly to get the correct hashes.
 */
public class GenerateHashes {
    public static void main(String[] args) {
        String[] pins = {"1234", "5678", "9999"};
        for (String pin : pins) {
            String hash = BCrypt.hashpw(pin, BCrypt.gensalt(12));
            System.out.println("PIN: " + pin + " -> " + hash);
        }
    }
}

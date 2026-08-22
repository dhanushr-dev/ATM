package com.oasisinfobyte.atm.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique transaction reference numbers.
 *
 * <p>Format: {@code TXN-yyMMddHHmm-NNNN} (max 19 chars, fits VARCHAR(20)).</p>
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.1.0
 */
public final class ReferenceGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyMMddHHmm");

    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

    /** Maximum length enforced by the {@code transactions.reference_number} column. */
    public static final int MAX_LENGTH = 20;

    /** Private constructor — utility class. */
    private ReferenceGenerator() {}

    /**
     * Generates a new unique reference number.
     *
     * @return reference string, e.g. {@code TXN-2408041751-0001}
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int    seq       = SEQUENCE.getAndIncrement() % 10000;
        String ref = String.format("TXN-%s-%04d", timestamp, seq);
        if (ref.length() > MAX_LENGTH) {
            throw new IllegalStateException("Reference number exceeds DB limit: " + ref);
        }
        return ref;
    }
}

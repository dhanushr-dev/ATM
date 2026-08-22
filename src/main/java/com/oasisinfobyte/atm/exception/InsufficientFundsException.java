package com.oasisinfobyte.atm.exception;

import java.math.BigDecimal;

/**
 * Thrown when an account has insufficient funds for a withdrawal or transfer.
 *
 * @author Oasis Infobyte ATM Project
 * @version 1.0.0
 */
public class InsufficientFundsException extends ATMException {

    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(ErrorCode.INSUFFICIENT_FUNDS,
              String.format("Insufficient funds. Available: ₹%,.2f, Requested: ₹%,.2f",
                            availableBalance, requestedAmount));
        this.availableBalance = availableBalance;
        this.requestedAmount  = requestedAmount;
    }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public BigDecimal getRequestedAmount()  { return requestedAmount; }
}

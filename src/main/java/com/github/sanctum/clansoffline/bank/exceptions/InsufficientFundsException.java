package com.github.sanctum.clansoffline.bank.exceptions;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Base class for insufficient funds-related exceptions.
 *
 * @since 1.0.0
 * @author ms5984
 */
public class InsufficientFundsException extends Exception {
    private static final long serialVersionUID = 8405404447841133055L;
    protected final BigDecimal amount;

    /**
     * Initialize the {@code amount} field to the provided amount.
     *
     * @param amount the desired amount
     */
    InsufficientFundsException(@NotNull BigDecimal amount) {
        this(amount, "Insufficient funds!");
    }

    /**
     * Initialize the {@code amount} field to the provided amount,
     * including a cause message.
     *
     * @param amount the desired amount
     * @param message a cause message
     */
    InsufficientFundsException(@NotNull BigDecimal amount, String message) {
        super(message);
        this.amount = amount;
    }

    /**
     * Initialize the {@code amount} field to the provided amount,
     * including a cause Throwable.
     *
     * @param amount the desired amount
     * @param cause a cause Throwable
     */
    InsufficientFundsException(@NotNull BigDecimal amount, Throwable cause) {
        super(cause);
        this.amount = amount;
    }

    /**
     * Initialize the {@code amount} field to the provided amount,
     * including a message and cause Throwable.
     *
     * @param amount the desired amount
     * @param message a cause message
     * @param cause a cause Throwable
     */
    InsufficientFundsException(@NotNull BigDecimal amount, String message, Throwable cause) {
        super(message, cause);
        this.amount = amount;
    }

    /**
     * Get the original amount in question.
     *
     * @return the original amount
     */
    public final BigDecimal getOriginalAmount() {
        return amount;
    }
}

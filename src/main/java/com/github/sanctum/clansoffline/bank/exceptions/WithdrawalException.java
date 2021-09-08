package com.github.sanctum.clansoffline.bank.exceptions;

import com.github.sanctum.clansoffline.bank.EconomyEntity;
import com.github.sanctum.clansoffline.bank.OfflineBank;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Thrown when there is an error withdrawing from an
 * {@link OfflineBank} or {@link EconomyEntity}.
 *
 * @since 1.0.0
 * @author ms5984
 */
public class WithdrawalException extends InsufficientFundsException {
    private static final long serialVersionUID = 5923125718270359142L;
    private static final String DEFAULT_MESSAGE = "Unable to withdraw %s";

    /**
     * Construct a withdrawal exception with the default message.
     *
     * @param amount the errant withdrawal amount
     */
    public WithdrawalException(@NotNull BigDecimal amount) {
        super(amount, String.format(DEFAULT_MESSAGE, amount));
    }

    /**
     * Construct a withdrawal exception with a custom message.
     *
     * @param amount the errant withdrawal amount
     * @param message a custom message
     */
    public WithdrawalException(@NotNull BigDecimal amount, @NotNull String message) {
        super(amount, message);
    }

    /**
     * Construct a withdrawal exception with the default message
     * and a cause Throwable.
     * <p>
     * Optionally, copy the Throwable message (if possible).
     *
     * @param amount the errant withdrawal amount
     * @param cause a cause throwable
     * @param copyMessage whether to copy the message from {@code cause}
     */
    public WithdrawalException(@NotNull BigDecimal amount, Throwable cause, boolean copyMessage) {
        super(amount, (copyMessage && cause != null) ? cause.getMessage() : String.format(DEFAULT_MESSAGE, amount), cause);
    }

    /**
     * Construct a withdrawal exception with a custom message
     * and a cause Throwable.
     *
     * @param amount the errant withdrawal amount
     * @param message a custom message
     * @param cause a cause Throwable
     */
    public WithdrawalException(@NotNull BigDecimal amount, @NotNull String message, Throwable cause) {
        super(amount, message, cause);
    }
}

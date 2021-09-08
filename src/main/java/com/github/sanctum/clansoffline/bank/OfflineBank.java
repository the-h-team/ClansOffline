package com.github.sanctum.clansoffline.bank;

import com.github.sanctum.clansoffline.bank.exceptions.DepositException;
import com.github.sanctum.clansoffline.bank.exceptions.WithdrawalException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Represents a clan bank.
 *
 * @since 1.0.0
 * @author ms5984
 */
public interface OfflineBank extends EconomyEntity {
    /**
     * Get the balance of the bank.
     *
     * @return the balance of the bank
     */
    @NotNull BigDecimal getBalance();

    /**
     * Deposit an amount in this bank by taking it from the provided entity.
     *
     * @param from a source entity
     * @param amount an amount
     * @throws WithdrawalException if {@code amount} cannot be taken
     */
    void depositFrom(@NotNull EconomyEntity from, @NotNull BigDecimal amount) throws WithdrawalException;

    /**
     * Withdraw an amount in this bank by giving it to the provided entity.
     *
     * @param to a destination entity
     * @param amount an amount
     * @throws WithdrawalException if the bank cannot produce {@code amount}
     * @throws DepositException if {@code amount} cannot be given to {@code to}
     */
    void withdrawTo(@NotNull EconomyEntity to, @NotNull BigDecimal amount) throws WithdrawalException, DepositException;
}

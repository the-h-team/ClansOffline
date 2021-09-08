package com.github.sanctum.clansoffline.bank;

import com.github.sanctum.clansoffline.bank.entities.EconomyProvisionPlayerEconomyEntity;
import com.github.sanctum.clansoffline.bank.exceptions.DepositException;
import com.github.sanctum.clansoffline.bank.exceptions.WithdrawalException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Abstracts any economy system to work with clan banks.
 *
 * @since 1.0.0
 * @author ms5984
 */
public interface EconomyEntity {
    /**
     * Get the name of this entity.
     *
     * @return the name of this entity
     */
    @NotNull String getName();

    /**
     * Check if this entity has the desired amount.
     *
     * @param amount an amount
     * @return true if the entity has the amount
     */
    boolean hasAmount(@NotNull BigDecimal amount);

    /**
     * Take an amount from this entity.
     *
     * @param amount an amount
     * @throws WithdrawalException if the amount could not be withdrawn
     */
    void takeAmount(@NotNull BigDecimal amount) throws WithdrawalException;

    /**
     * Give an amount to this entity.
     *
     * @param amount an amount
     * @throws DepositException if the amount could not be given
     */
    void giveAmount(@NotNull BigDecimal amount) throws DepositException;

    /**
     * Check if this entity has the desired amount as a double.
     *
     * @deprecated prefer the BigDecimal-based methods
     * @param doubleAmount a double amount
     * @return true if the entity has the amount
     */
    @Deprecated
    default boolean hasAmount(double doubleAmount) {
        return hasAmount(BigDecimal.valueOf(doubleAmount));
    }

    /**
     * Take an amount (as a double) from this entity.
     *
     * @deprecated prefer the BigDecimal-based methods
     * @param doubleAmount a double amount
     * @throws WithdrawalException if the amount could not be withdrawn
     */
    @Deprecated
    default void takeAmount(double doubleAmount) throws WithdrawalException {
        takeAmount(BigDecimal.valueOf(doubleAmount));
    }

    /**
     * Give an amount (as a double) to this entity.
     *
     * @deprecated prefer the BigDecimal-based methods
     * @param doubleAmount a double amount
     * @throws DepositException if the amount could not be given
     */
    @Deprecated
    default void giveAmount(double doubleAmount) throws DepositException {
        giveAmount(BigDecimal.valueOf(doubleAmount));
    }

    /**
     * Get an EconomyEntity for a given player using EconomyProvision.
     *
     * @param player a player
     * @return an economy entity
     * @implNote This implementation is based on Labyrinth's
     *           EconomyProvision utility and thus functions
     *           with either Vault or Enterprise providers.
     */
    static EconomyEntity ofPlayerByProvision(@NotNull Player player) {
        return new EconomyProvisionPlayerEconomyEntity(player);
    }
}

package com.github.sanctum.clansoffline.bank.entities;

import com.github.sanctum.clansoffline.bank.EconomyEntity;
import com.github.sanctum.clansoffline.bank.exceptions.DepositException;
import com.github.sanctum.clansoffline.bank.exceptions.WithdrawalException;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * An implementation of EconomyEntity using Labyrinth's EconomyProvision
 * utility to enable Vault and Enterprise support.
 *
 * @since 1.0.0
 * @author ms5984
 */
public class EconomyProvisionPlayerEconomyEntity implements EconomyEntity {
    private final OfflinePlayer player;

    public EconomyProvisionPlayerEconomyEntity(@NotNull OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public @NotNull String getName() {
        String name = player.getName();
        if (name == null) return "UnknownPlayer[" + player.getUniqueId() + "]";
        return name;
    }

    @Override
    public boolean hasAmount(@NotNull BigDecimal amount) {
        return EconomyProvision.getInstance().has(amount, player).orElse(false);
    }

    @Override
    public void takeAmount(@NotNull BigDecimal amount) throws WithdrawalException {
        if (!EconomyProvision.getInstance().withdraw(amount, player).orElseThrow(() -> withdrawError(amount))) {
            throw withdrawError(amount);
        }
    }

    @Override
    public void giveAmount(@NotNull BigDecimal amount) throws DepositException {
        if (!EconomyProvision.getInstance().deposit(amount, player).orElseThrow(() -> depositError(amount))) {
            throw depositError(amount);
        }
    }

    private WithdrawalException withdrawError(BigDecimal amount) {
        return new WithdrawalException(amount, "Unable to withdraw " + amount + " from " + getName());
    }

    private DepositException depositError(BigDecimal amount) {
        return new DepositException(amount, "Unable to deposit " + amount + " to " + getName());
    }
}

package com.github.sanctum.clansoffline.bank;

import com.github.sanctum.clansoffline.bank.data.BalanceDatastore;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages clan bank data.
 *
 * @since 1.0.0
 * @author ms5984
 */
public class BankManager {
    /**
     * Datafile's path in relation to the plugin's data folder.
     */
    public static final String DATAFILE_REL_PATH = "Data/banks.json";
    final File dataFile;
    final BalanceDatastore balanceDatastore = new BalanceDatastore(ConcurrentHashMap::new);
    final AtomicReference<BigDecimal> startingBalance = new AtomicReference<>(BigDecimal.ZERO);

    public BankManager(@NotNull Plugin plugin) {
        final File pluginDataFolder = plugin.getDataFolder();
        this.dataFile = new File(pluginDataFolder, DATAFILE_REL_PATH);
        final String readablePath = pluginDataFolder.getPath() + '/' + DATAFILE_REL_PATH;
        if (!this.dataFile.isFile()) {
            if (this.dataFile.isDirectory()) {
                throw new IllegalStateException(readablePath + " cannot be a directory");
            }
            //noinspection ResultOfMethodCallIgnored
            dataFile.getParentFile().mkdirs();
            try {
                if (dataFile.createNewFile()) {
                    plugin.getLogger().info(() -> "Created a new bank datafile! Location " + readablePath);
                }
                return;
            } catch (IOException e) {
                throw new IllegalStateException("A file error has occurred while accessing " + readablePath, e);
            }
        }
        try {
            balanceDatastore.awaitLoadFromReader(new FileReader(this.dataFile));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Set a custom starting balance for uninitialized clan banks.
     * <p>
     * <b>Default: {@linkplain BigDecimal#ZERO}</b>
     *
     * @param newDefaultBalance a new default balance
     */
    public void setStartingBalance(@NotNull BigDecimal newDefaultBalance) {
        this.startingBalance.set(newDefaultBalance);
    }

    /**
     * Get the starting balance for uninitialized clan banks.
     *
     * @return the starting balance
     * @implNote Initialized on load to {@linkplain BigDecimal#ZERO}.
     */
    public @NotNull BigDecimal getStartingBalance() {
        return this.startingBalance.get();
    }

    /**
     * Get the bank balance for a given clan id.
     *
     * @param clanId the clan's clan id
     * @return the clan bank balance
     */
    public @NotNull BigDecimal getBalance(@NotNull HUID clanId) {
        final String toString = balanceDatastore.get(clanId.toString()).join();
        if (toString != null) {
            return new BigDecimal(toString);
        }
        final BigDecimal startingBalance = getStartingBalance();
        setBalance(clanId, startingBalance);
        return startingBalance;
    }

    /**
     * Set the bank balance for a given clan id.
     * <p>
     * {@code newBalance = null} can be used to clear the existing data.
     *
     * @param clanId the clan's clan id
     * @param newBalance the new balance
     */
    public void setBalance(@NotNull HUID clanId, @Nullable BigDecimal newBalance) {
        if (newBalance != null) {
            balanceDatastore.putNow(clanId.toString(), newBalance.toString());
        } else {
            balanceDatastore.removeNow(clanId.toString());
        }
        balanceDatastore.saveToFileAsync(dataFile);
    }

    /**
     * Adjust the bank balance for a given clan id.
     *
     * @param clanId the clan's clan id
     * @param adjustment the BigDecimal amount to adjust by (signed)
     */
    public void adjustBalance(@NotNull HUID clanId, @NotNull BigDecimal adjustment) {
        setBalance(clanId, getBalance(clanId).add(adjustment));
    }

    /**
     * Safely shutdown the manager, waiting for internal datastore tasks.
     *
     * @param plugin plugin for logging
     */
    public void safeShutdown(@NotNull Plugin plugin) {
        balanceDatastore.saveToFileAsync(dataFile);
        balanceDatastore.awaitShutdown(plugin);
    }
}

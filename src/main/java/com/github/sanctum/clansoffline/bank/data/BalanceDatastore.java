package com.github.sanctum.clansoffline.bank.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A datastore for all clan banks.
 *
 * @since 1.0.0
 * @author ms5984
 */
public class BalanceDatastore extends JSONKeyValueDatastore<String, String> {
    /**
     * Initialize a new datastore using the specified Map constructor.
     *
     * @param generatorFunction a Map constructor
     */
    public <T extends Map<String, String>> BalanceDatastore(@NotNull Supplier<T> generatorFunction) {
        super(generatorFunction);
    }
}

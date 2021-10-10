package com.github.sanctum.clansoffline.bank.data;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Store and load key-value mappings of arbitrary type using JSON.
 * <p>
 * Supports export to String, files and any Appendables;
 * supports loading from String and Readers.
 *
 * @since 1.0.0
 * @author ms5984
 *
 * @param <K> key type
 * @param <V> value type
 */
@SuppressWarnings("UnusedReturnValue")
public class JSONKeyValueDatastore<K, V> {
    protected final Gson gson;
    protected final Map<K, V> data;
    protected final Type type;
    protected final ExecutorService readWriteService;
    protected final ExecutorService loadSaveService;
    protected final String datastoreName;

    /**
     * Initialize a new datastore using the specified Map constructor
     * to instantiate the internal backing collection.
     * <p>
     * Delegates to {@link #JSONKeyValueDatastore(Supplier, Gson)} with
     * {@code gson} instantiated with {@code new Gson()}.
     *
     * @param generatorFunction a Map constructor
     * @param <T> inferred Map subtype
     */
    public <T extends Map<K, V>> JSONKeyValueDatastore(@NotNull Supplier<T> generatorFunction) {
        this(generatorFunction, new Gson());
    }

    /**
     * Initialize a new datastore using the specified Map constructor
     * to instantiate the internal backing collection and {@code gson}
     * to control serialization behavior
     *
     * @param generatorFunction a Map constructor
     * @param gson a customized Gson instance
     * @param <T> inferred Map subtype
     */
    public <T extends Map<K, V>> JSONKeyValueDatastore(@NotNull Supplier<T> generatorFunction, @NotNull Gson gson) {
        this.gson = gson;
        data = generatorFunction.get();
        type = new TypeToken<T>() {}.getType();
        readWriteService = Executors.newSingleThreadExecutor();
        loadSaveService = Executors.newSingleThreadExecutor();
        if (!getClass().isAnonymousClass()) {
            datastoreName = getClass().getSimpleName();
        } else {
            datastoreName = getClass().getSuperclass().getSimpleName();
        }
    }

    // await + async r/w

    /**
     * Get the value at the provided key.
     *
     * @param key a valid key
     * @return value or null
     * @throws NullPointerException if internal Map does not accept null keys
     */
    public @Nullable V getNow(K key) throws NullPointerException {
        try {
            return get(key).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Get the value at the provided key expressed as the result of a
     * CompletableFuture.
     *
     * @param key a valid key
     * @return a CompletableFuture describing the result
     * @implNote Delegates to {@link Map#get(K)}, and as such may complete
     *           exceptionally with {@link NullPointerException} if the
     *           internal Map is not tolerant of null keys.
     */
    public CompletableFuture<V> get(K key) {
        return CompletableFuture.supplyAsync(() -> data.get(key), readWriteService);
    }

    /**
     * Assign a new value to a given key.
     *
     * @param key a valid key
     * @param newValue a new value
     * @return old value (could be null)
     * @throws NullPointerException if internal Map does not accept null keys
     * @throws IllegalArgumentException if the key or value is inappropriate
     *         for the internal Map
     */
    public @Nullable V putNow(K key, V newValue) throws NullPointerException, IllegalArgumentException {
        try {
            return put(key, newValue).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Assign a new value to a given key, expressing the success of the update
     * as null, the previous value or an exceptional case.
     *
     * @param key a valid key
     * @param newValue a new value
     * @return a CompletableFuture describing the result
     * @implNote Delegates to {@link Map#put(K, V)}, and as such may complete
     *           exceptionally with the following causes:
     *           <ul>
     *               <li>{@link NullPointerException}
     *               <p>if the internal map is not tolerant of null keys
     *               </li>
     *               <li>{@link IllegalArgumentException}
     *               <p>if the the key or value is inappropriate for the
     *               internal Map
     *               </li>
     *           </ul>
     */
    public CompletableFuture<V> put(K key, V newValue) {
        return CompletableFuture.supplyAsync(() -> data.put(key, newValue), readWriteService);
    }

    /**
     * Remove the value of a given key (if present).
     *
     * @param key a valid key
     * @return old value (could be null)
     * @throws NullPointerException if internal Map does not accept null keys
     */
    public @Nullable V removeNow(K key) throws NullPointerException {
        try {
            return remove(key).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Remove the value of a given key (if present), expressing the result as
     * the previous value or an exceptional case.
     *
     * @param key a valid key
     * @return a CompletableFuture describing the result
     * @implNote Delegates to {@link Map#remove(K)}, and as such may complete
     *           exceptionally with the following cause:
     *           <ul>
     *               <li>{@link NullPointerException}
     *               <p>if the internal map is not tolerant of null keys
     *               </li>
     *           </ul>
     */
    public CompletableFuture<V> remove(K key) {
        return CompletableFuture.supplyAsync(() -> data.remove(key), readWriteService);
    }

    // await and async i/o

    /**
     * Save the contents of the datastore to a JSON string.
     * <p>
     * <b>Blocks until the operation is complete.</b>
     *
     * @return a json string representing contents of this datastore
     * @throws CancellationException if the operation was cancelled internally
     */
    public @NotNull String awaitSaveToString() throws CancellationException {
        return saveToStringAsync().join();
    }

    /**
     * Save the contents of the datastore to a JSON string.
     *
     * @return a CompletableFuture describing the result
     */
    public CompletableFuture<String> saveToStringAsync() {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (data) {
                return gson.toJson(data, type);
            }
        }, loadSaveService);
    }

    /**
     * Save the contents of the datastore to a .json file.
     * <p>
     * <b>Blocks until the operation is complete.</b>
     *
     * @param file a valid file
     * @throws IllegalArgumentException if {@code file} does not represent a
     *         valid .json that exists or can be created
     * @throws JsonIOException if Gson is unable to write to the FileWriter
     * @throws CancellationException if the operation was cancelled internally
     */
    public void awaitSaveToFile(@NotNull File file) throws IllegalArgumentException, JsonIOException, CancellationException {
        try {
            saveToFileAsync(file).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Save the contents of the datastore to a .json file.
     *
     * @param file a valid file
     * @return a CompletableFuture describing the result
     */
    public CompletableFuture<Void> saveToFileAsync(@NotNull File file) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (!file.isFile() && !file.createNewFile()) {
                    throw new IllegalArgumentException("'file' must be a valid .json file!");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to create file!", e);
            }
            if (!file.getName().endsWith(".json")) {
                throw new IllegalArgumentException("File should end in .json");
            }
            try (FileWriter writer = new FileWriter(file)) {
                synchronized (data) {
                    gson.toJson(data, type, writer);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, loadSaveService);
    }

    /**
     * Save the contents of the datastore to an {@link Appendable}.
     * <p>
     * <b>Blocks until the operation is complete.</b>
     *
     * @param appendable an appendable
     * @throws JsonIOException if Gson is unable to write to the FileWriter
     * @throws CancellationException if the operation was cancelled internally
     */
    public void awaitSaveAppendable(@NotNull Appendable appendable) throws JsonIOException, CancellationException {
        try {
            saveAppendableAsync(appendable).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Save the contents of the datastore to an {@link Appendable}.
     *
     * @param appendable an appendable
     * @return a CompletableFuture describing the result
     */
    public CompletableFuture<Void> saveAppendableAsync(@NotNull Appendable appendable) {
        return CompletableFuture.runAsync(() -> {
            synchronized (data) {
                gson.toJson(data, type, appendable);
            }
        }, loadSaveService);
    }

    /**
     * Replace contents of the datastore using a JSON string.
     * <p>
     * <b>Blocks until the operation is complete.</b>
     *
     * @param json a valid JSON representation of datastore contents
     * @throws JsonParseException if the string is an invalid representation
     * @throws JsonSyntaxException if the string is malformed json
     * @throws CancellationException if the operation was cancelled internally
     */
    public void awaitLoadFromString(@NotNull String json) throws JsonParseException, JsonSyntaxException, CancellationException {
        try {
            loadFromStringAsync(json).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Replace contents of the datastore using a JSON string.
     *
     * @param json a valid JSON representation of datastore contents
     * @return a CompletableFuture describing the result
     */
    public CompletableFuture<Void> loadFromStringAsync(@NotNull String json) {
        return CompletableFuture.runAsync(() -> {
            final Map<K, V> fromJson = gson.fromJson(json, type);
            synchronized (data) {
                data.clear();
                data.putAll(fromJson);
            }
        }, loadSaveService);
    }

    /**
     * Replace contents of the datastore using a {@link Reader}.
     * <p>
     * <b>Blocks until the operation is complete.</b>
     *
     * @param reader a reader
     * @throws JsonIOException if there was a problem reading from the reader
     * @throws JsonSyntaxException if the Reader output is malformed json
     * @throws CancellationException if the operation was cancelled internally
     */
    public void awaitLoadFromReader(@NotNull Reader reader) throws JsonIOException, JsonSyntaxException, CancellationException {
        try {
            loadFromReaderAsync(reader).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    /**
     * Replace contents of the datastore using a {@link Reader}
     *
     * @param reader a reader
     * @return a CompletableFuture describing the result
     */
    public CompletableFuture<Void> loadFromReaderAsync(@NotNull Reader reader) {
        return CompletableFuture.runAsync(() -> {
            final Map<K, V> fromJson = gson.fromJson(reader, type);
            synchronized (data) {
                data.clear();
                data.putAll(fromJson);
            }
        }, loadSaveService);
    }

    /**
     * Await safe shutdown of all datastore tasks.
     * <p>
     * The plugin is used for logging.
     *
     * @param plugin a plugin instance for logging purposes
     */
    public void awaitShutdown(@NotNull Plugin plugin) {
        readWriteService.shutdown();
        try {
            plugin.getLogger().info(() -> "Waiting for " + datastoreName + " access tasks...");
            if (readWriteService.awaitTermination(7L, TimeUnit.SECONDS)) {
                plugin.getLogger().info(() -> datastoreName + ": Access tasks complete.");
            } else {
                plugin.getLogger().warning(() -> datastoreName + " access tasks cancelled due to timeout.");
            }
        } catch (InterruptedException e) {
            plugin.getLogger().severe(e::getMessage);
            plugin.getLogger().severe(() -> datastoreName + " access tasks interrupted.");
        }
        loadSaveService.shutdown();
        try {
            plugin.getLogger().info(() -> "Waiting for " + datastoreName + " load and save tasks...");
            if (readWriteService.awaitTermination(10L, TimeUnit.SECONDS)) {
                plugin.getLogger().info(() -> datastoreName + ": Load and save tasks complete.");
            } else {
                plugin.getLogger().severe(() -> datastoreName + " load and save tasks cancelled due to timeout.");
            }
        } catch (InterruptedException e) {
            plugin.getLogger().severe(e::getMessage);
            plugin.getLogger().severe(() -> datastoreName + " save/load tasks interrupted.");
        }
    }
}

package jp.jyn.jecon.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

public interface BalanceRepository {

    /**
     * Format the amount
     *
     * @param value value
     * @return Formatted value
     */
    String format(double value);

    /**
     * Format the amount
     *
     * @param value value
     * @return Formatted value
     */
    String format(BigDecimal value);

    /**
     * Format the player balance
     *
     * @param uuid Target uuid
     * @return Formatted balance, empty if no player exists
     */
    Optional<String> format(UUID uuid);

    /**
     * Get balance with double
     *
     * @param uuid Target uuid
     * @return Balance, empty if account does not exist
     */
    OptionalDouble getDouble(UUID uuid);

    /**
     * Get balance with BigDecimal
     *
     * @param uuid Target uuid
     * @return Balance, empty if account does not exist
     */
    Optional<BigDecimal> getDecimal(UUID uuid);

    /**
     * Alias of {@link BalanceRepository#getDecimal(UUID)}
     *
     * @param uuid Target uuid
     * @return Balance, empty if account does not exist
     */
    default Optional<BigDecimal> get(UUID uuid) {
        return getDecimal(uuid);
    }

    /**
     * Set balance
     *
     * @param uuid    Target uuid
     * @param balance New balance
     * @return true if it was successfully updated, false otherwise (eg account does not exist)
     */
    boolean set(UUID uuid, double balance);

    /**
     * Set balance
     *
     * @param uuid    Target uuid
     * @param balance New balance
     * @return true if it was successfully updated, false otherwise (eg account does not exist)
     */
    boolean set(UUID uuid, BigDecimal balance);

    /**
     * Is the balance greater than the specified value?
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if balance is enough
     */
    boolean has(UUID uuid, double amount);

    /**
     * Is the balance greater than the specified value?
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if balance is enough
     */
    boolean has(UUID uuid, BigDecimal amount);

    /**
     * Deposit with double
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if deposit was successful, false otherwise (eg no account exists)
     */
    boolean deposit(UUID uuid, double amount);

    /**
     * Deposit with BigDecimal
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if deposit was successful, false otherwise (eg no account exists)
     */
    boolean deposit(UUID uuid, BigDecimal amount);

    /**
     * Withdraw with double
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if withdraw was successful, false otherwise (eg no account exists)
     */
    boolean withdraw(UUID uuid, double amount);

    /**
     * Withdraw with BigDecimal
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if withdraw was successful, false otherwise (eg no account exists)
     */
    boolean withdraw(UUID uuid, BigDecimal amount);

    /**
     * Does the account exist?
     *
     * @param uuid Target uuid
     * @return true if it exists
     */
    boolean hasAccount(UUID uuid);

    /**
     * Create account
     *
     * @param uuid    Target uuid
     * @param balance balance
     * @return true if creation succeeded, otherwise false (eg account already exists)
     */
    boolean createAccount(UUID uuid, double balance);

    /**
     * Create account
     *
     * @param uuid    Target uuid
     * @param balance balance
     * @return true if creation succeeded, otherwise false (eg account already exists)
     */
    boolean createAccount(UUID uuid, BigDecimal balance);

    /**
     * Remove account
     *
     * @param uuid Target uuid
     * @return true if removed, false otherwise (eg account does not exist)
     */
    boolean removeAccount(UUID uuid);

    /**
     * Get the billionaires ranking.
     *
     * @param limit  limit
     * @param offset offset
     * @return Map of Billionaires ranking (This is usually, LinkedHashMap to preserve the order)
     */
    Map<UUID, BigDecimal> top(int limit, int offset);
}

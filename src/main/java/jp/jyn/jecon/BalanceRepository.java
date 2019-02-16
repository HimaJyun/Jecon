package jp.jyn.jecon;

import jp.jyn.jbukkitlib.cache.NoOpMap;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.util.PackagePrivate;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.db.Database;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class BalanceRepository {
    public final static int FRACTIONAL_DIGITS = 2;
    private final static int MULTIPLIER = 100;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private final StringVariable variable = StringVariable.init();

    private final Database db;
    private final boolean lazyWrite;
    private final MainConfig.FormatConfig formatConfig;
    public final BigDecimal defaultBalance;

    private final Map<UUID, Integer> uuidToIdCache;
    private final Map<Integer, OptionalLong> idToBalanceCache;

    @PackagePrivate
    BalanceRepository(MainConfig config, Database db) {
        this.db = db;
        this.defaultBalance = config.defaultBalance;
        this.lazyWrite = config.lazyWrite;
        formatConfig = config.format;

        uuidToIdCache = new HashMap<>();
        idToBalanceCache = lazyWrite ? new HashMap<>() : NoOpMap.getInstance();
    }

    private long double2long(double value) {
        return (long) (value * MULTIPLIER);
    }

    private long decimal2long(BigDecimal value) {
        return value.scaleByPowerOfTen(FRACTIONAL_DIGITS).longValue();
    }

    private String format(long value) {
        long major = value / MULTIPLIER;
        long minor = value % MULTIPLIER;
        TemplateVariable v = variable.clear()
            .put("major", numberFormat.format(major))
            .put("minor", minor)
            .put("majorcurrency", major > 1 ? formatConfig.pluralMajor : formatConfig.singularMajor)
            .put("minorcurrency", minor > 1 ? formatConfig.pluralMinor : formatConfig.singularMinor);

        return (minor == 0 ? formatConfig.formatZeroMinor : formatConfig.format).toString(v);
    }

    /**
     * Format the amount
     *
     * @param value value
     * @return Formatted value
     */
    public String format(double value) {
        return format(double2long(value));
    }

    /**
     * Format the amount
     *
     * @param value value
     * @return Formatted value
     */
    public String format(BigDecimal value) {
        return format(decimal2long(value));
    }

    /**
     * Form the player balance
     *
     * @param uuid Target uuid
     * @return Formatted balance, empty if no player exists
     */
    public Optional<String> format(UUID uuid) {
        OptionalLong balance = getRawBalance(getId(uuid));
        if (balance.isPresent()) {
            return Optional.of(format(balance.getAsLong()));
        }
        return Optional.empty();
    }

    private Integer getId(UUID uuid) {
        Integer id = uuidToIdCache.get(uuid);
        if (id != null) {
            return id;
        }

        id = db.getId(uuid);
        uuidToIdCache.put(uuid, id);
        return id;
    }

    public boolean save(UUID uuid) {
        Integer id = getId(uuid);
        OptionalLong balance = idToBalanceCache.remove(id);
        if (balance != null && balance.isPresent()) {
            return db.setCreate(id, balance.getAsLong());
        }
        return false;
    }

    public void saveAll() {
        idToBalanceCache.entrySet().removeIf(e -> {
            Integer id = e.getKey();
            OptionalLong balance = e.getValue();
            if (balance != null && balance.isPresent()) {
                return db.setCreate(id, balance.getAsLong());
            }
            return false;
        });
    }

    private OptionalLong getRawBalance(Integer id) {
        return idToBalanceCache.computeIfAbsent(id, db::getBalance);
    }

    /**
     * Get balance with double
     *
     * @param uuid Target uuid
     * @return Balance, empty if account does not exist
     */
    public OptionalDouble getDouble(UUID uuid) {
        OptionalLong v = getRawBalance(getId(uuid));
        if (v.isPresent()) {
            return OptionalDouble.of((double) v.getAsLong() / MULTIPLIER);
        } else {
            return OptionalDouble.empty();
        }
    }

    /**
     * Get balance with BigDecimal
     *
     * @param uuid Target uuid
     * @return Balance, empty if account does not exist
     */
    public Optional<BigDecimal> getDecimal(UUID uuid) {
        OptionalLong v = getRawBalance(getId(uuid));
        if (v.isPresent()) {
            return Optional.of(BigDecimal.valueOf(v.getAsLong()).scaleByPowerOfTen(-FRACTIONAL_DIGITS));
        } else {
            return Optional.empty();
        }
    }

    private boolean deposit(UUID uuid, long amount) {
        Integer id = getId(uuid);
        if (!lazyWrite) {
            if (!db.deposit(id, amount)) {
                return false;
            }
        }

        OptionalLong v = idToBalanceCache.get(id);
        if (v != null && v.isPresent()) {
            idToBalanceCache.put(id, OptionalLong.of(v.getAsLong() + amount));
        }

        return true;
    }

    /**
     * Deposit with double
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if deposit was successful, false otherwise (eg no account exists)
     */
    public boolean deposit(UUID uuid, double amount) {
        return this.deposit(uuid, double2long(amount));
    }

    /**
     * Deposit with BigDecimal
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if deposit was successful, false otherwise (eg no account exists)
     */
    public boolean deposit(UUID uuid, BigDecimal amount) {
        return this.deposit(uuid, decimal2long(amount));
    }

    /**
     * Withdraw with double
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if withdraw was successful, false otherwise (eg no account exists)
     */
    public boolean withdraw(UUID uuid, double amount) {
        return this.deposit(uuid, -double2long(amount));
    }

    /**
     * Withdraw with BigDecimal
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if withdraw was successful, false otherwise (eg no account exists)
     */
    public boolean withdraw(UUID uuid, BigDecimal amount) {
        return this.deposit(uuid, -decimal2long(amount));
    }

    private boolean hasAccount(Integer id) {
        return getRawBalance(id).isPresent();
    }

    /**
     * Does the account exist?
     *
     * @param uuid Target uuid
     * @return true if it exists
     */
    public boolean hasAccount(UUID uuid) {
        return hasAccount(getId(uuid));
    }

    private boolean createAccount(UUID uuid, long balance) {
        Integer id = getId(uuid);
        if (hasAccount(id)) {
            return false;
        }

        if (!lazyWrite) {
            if (!db.createAccount(id, balance)) {
                return false;
            }
        }

        idToBalanceCache.put(id, OptionalLong.of(balance));
        return true;
    }

    /**
     * Create account
     *
     * @param uuid    Target uuid
     * @param balance balance
     * @return true if creation succeeded, otherwise false (eg account already exists)
     */
    public boolean createAccount(UUID uuid, double balance) {
        return createAccount(uuid, double2long(balance));
    }

    /**
     * Create account
     *
     * @param uuid    Target uuid
     * @param balance balance
     * @return true if creation succeeded, otherwise false (eg account already exists)
     */
    public boolean createAccount(UUID uuid, BigDecimal balance) {
        return createAccount(uuid, decimal2long(balance));
    }

    /**
     * Remove account
     *
     * @param uuid Target uuid
     * @return true if removed, false otherwise (eg account does not exist)
     */
    public boolean removeAccount(UUID uuid) {
        Integer id = getId(uuid);
        idToBalanceCache.remove(id);
        return db.removeAccount(id);
    }

    private boolean has(UUID uuid, long amount) {
        OptionalLong balance = getRawBalance(getId(uuid));
        if (!balance.isPresent()) {
            return false;
        }
        return balance.getAsLong() >= amount;
    }

    /**
     * Is the balance greater than the specified value?
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if balance is enough
     */
    public boolean has(UUID uuid, double amount) {
        return has(uuid, (long) amount * MULTIPLIER);
    }

    /**
     * Is the balance greater than the specified value?
     *
     * @param uuid   Target uuid
     * @param amount amount
     * @return true if balance is enough
     */
    public boolean has(UUID uuid, BigDecimal amount) {
        return has(uuid, amount.scaleByPowerOfTen(FRACTIONAL_DIGITS).longValue());
    }

    private boolean set(UUID uuid, long balance) {
        Integer id = getId(uuid);
        if (!lazyWrite) {
            if (!db.setBalance(id, balance)) {
                return false;
            }
        }

        idToBalanceCache.put(id, OptionalLong.of(balance));
        return true;
    }

    /**
     * Set balance
     *
     * @param uuid    Target uuid
     * @param balance New balance
     * @return true if it was successfully updated, false otherwise (eg account does not exist)
     */
    public boolean set(UUID uuid, double balance) {
        return set(uuid, double2long(balance));
    }

    /**
     * Set balance
     *
     * @param uuid    Target uuid
     * @param balance New balance
     * @return true if it was successfully updated, false otherwise (eg account does not exist)
     */
    public boolean set(UUID uuid, BigDecimal balance) {
        return set(uuid, decimal2long(balance));
    }

    public Map<UUID, BigDecimal> top(int limit, int offset) {
        Map<UUID, BigDecimal> result = new LinkedHashMap<>();

        // This is a heavy load.
        // But, it is not a frequently executed process, so there is no problem.
        HashMap<Integer, UUID> idToUUID = new HashMap<>(uuidToIdCache.size() * 4 / 3);
        uuidToIdCache.forEach((key, value) -> idToUUID.put(value, key));

        db.top(limit, offset).forEach((id, balance) -> {
            UUID uuid = idToUUID.get(id);
            if (uuid == null) {
                uuid = db.getUUID(id).orElse(null);
            }
            result.put(uuid, BigDecimal.valueOf(balance).scaleByPowerOfTen(-FRACTIONAL_DIGITS));
        });

        return result;
    }
}

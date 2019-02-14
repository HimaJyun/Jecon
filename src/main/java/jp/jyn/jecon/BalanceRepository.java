package jp.jyn.jecon;

import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.db.Database;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.UUID;

public class BalanceRepository {
    public enum Result {SUCCESS, ACCOUNT_NOT_FOUND}

    public final static int FRACTIONAL_DIGITS = 2;
    private final static int MULTIPLIER = 100;
    private final StringVariable variable = StringVariable.init();

    private final MainConfig config;
    private final Database db;

    private final Map<UUID, Integer> uuidToIdCache;
    private final Map<Integer, OptionalLong> idToBalanceCache;

    public BalanceRepository(MainConfig config, Database db) {
        this.config = config;
        this.db = db;
        uuidToIdCache = config.cache.id.create(false);
        idToBalanceCache = config.cache.balance.create(false);
    }

    private long double2long(double value) {
        return (long) (value * MULTIPLIER);
    }

    private long decimal2long(BigDecimal value) {
        return value.scaleByPowerOfTen(FRACTIONAL_DIGITS).longValue();
    }

    private String format(long value) {
        final MainConfig.FormatConfig f = config.format;
        long major = value / MULTIPLIER;
        long minor = value % MULTIPLIER;
        TemplateVariable v = variable.clear()
            .put("major", major)
            .put("minor", minor)
            .put("majorcurrency", major > 1 ? f.pluralMajor : f.singularMajor)
            .put("minorcurrency", minor > 1 ? f.pluralMinor : f.singularMinor);

        return (minor == 0 ? f.formatZeroMinor : f.format).toString(v);
    }

    public String format(double value) {
        return format(double2long(value));
    }

    public String format(BigDecimal value) {
        return format(decimal2long(value));
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

    private OptionalLong getRawBalance(Integer id) {
        return idToBalanceCache.computeIfAbsent(id, db::getBalance);
    }

    public OptionalDouble getDouble(UUID uuid) {
        OptionalLong v = getRawBalance(getId(uuid));
        if (v.isPresent()) {
            return OptionalDouble.of((double) v.getAsLong() / MULTIPLIER);
        } else {
            return OptionalDouble.empty();
        }
    }

    public Optional<BigDecimal> getDecimal(UUID uuid) {
        OptionalLong v = getRawBalance(getId(uuid));
        if (v.isPresent()) {
            return Optional.of(BigDecimal.valueOf(v.getAsLong()).scaleByPowerOfTen(-FRACTIONAL_DIGITS));
        } else {
            return Optional.empty();
        }
    }

    private Result deposit(UUID uuid, long amount) {
        Integer id = getId(uuid);
        if (!db.deposit(id, amount)) {
            return Result.ACCOUNT_NOT_FOUND;
        }

        OptionalLong v = idToBalanceCache.get(id);
        if (v.isPresent()) {
            idToBalanceCache.put(id, OptionalLong.of(v.getAsLong() + amount));
        }

        return Result.SUCCESS;
    }

    public Result deposit(UUID uuid, double amount) {
        return this.deposit(uuid, double2long(amount));
    }

    public Result deposit(UUID uuid, BigDecimal amount) {
        return this.deposit(uuid, decimal2long(amount));
    }

    public Result withdraw(UUID uuid, double amount) {
        return this.deposit(uuid, -double2long(amount));
    }

    public Result withdraw(UUID uuid, BigDecimal amount) {
        return this.deposit(uuid, -decimal2long(amount));
    }

    private boolean hasAccount(Integer id) {
        return getRawBalance(id).isPresent();
    }

    public boolean hasAccount(UUID uuid) {
        return hasAccount(getId(uuid));
    }

    private boolean createAccount(UUID uuid, long balance) {
        Integer id = getId(uuid);
        if (!hasAccount(id) && db.createAccount(id, balance)) {
            idToBalanceCache.put(id, OptionalLong.of(balance));
            return true;
        }

        return false;
    }

    public boolean createAccount(UUID uuid, double balance) {
        return createAccount(uuid, double2long(balance));
    }

    public boolean createAccount(UUID uuid, BigDecimal balance) {
        return createAccount(uuid, decimal2long(balance));
    }

    public boolean createAccount(UUID uuid) {
        return createAccount(uuid, config.defaultBalance);
    }

    public boolean removeAccount(UUID uuid) {
        Integer id = getId(uuid);
        idToBalanceCache.remove(id);
        return db.removeAccount(id);
    }

    public boolean has(UUID uuid, double amount) {
        return getDouble(uuid).orElse(0) >= amount;
    }

    public boolean has(UUID uuid, BigDecimal amount) {
        return getDecimal(uuid).orElse(BigDecimal.ZERO).compareTo(amount) > -1;
    }

    private boolean set(UUID uuid, long balance) {
        Integer id = getId(uuid);
        if (db.setBalance(id, balance)) {
            idToBalanceCache.put(id, OptionalLong.of(balance));
            return true;
        }
        return false;
    }

    public boolean set(UUID uuid, double balance) {
        return set(uuid, double2long(balance));
    }

    public boolean set(UUID uuid, BigDecimal balance) {
        return set(uuid, decimal2long(balance));
    }
}

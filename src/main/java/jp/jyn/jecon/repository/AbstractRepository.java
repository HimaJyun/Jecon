package jp.jyn.jecon.repository;

import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
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

public abstract class AbstractRepository implements BalanceRepository {
    public final static int FRACTIONAL_DIGITS = 2;
    protected final static int MULTIPLIER = 100;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private final StringVariable variable = StringVariable.init();

    protected final Database db;
    private final MainConfig.FormatConfig formatConfig;
    private final Map<UUID, Integer> uuidToIdCache;

    protected AbstractRepository(MainConfig config, Database db) {
        this.db = db;
        formatConfig = config.format;

        uuidToIdCache = new HashMap<>();
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

    protected final Integer getId(UUID uuid) {
        return uuidToIdCache.computeIfAbsent(uuid, db::getId);
    }

    @Override
    public final String format(double value) {
        return format(double2long(value));
    }

    @Override
    public final String format(BigDecimal value) {
        return format(decimal2long(value));
    }

    @Override
    public final Optional<String> format(UUID uuid) {
        OptionalLong balance = getRaw(uuid);
        if (balance.isPresent()) {
            return Optional.of(format(balance.getAsLong()));
        }
        return Optional.empty();
    }

    protected abstract OptionalLong getRaw(UUID uuid);

    @Override
    public final OptionalDouble getDouble(UUID uuid) {
        OptionalLong v = getRaw(uuid);
        if (v.isPresent()) {
            return OptionalDouble.of((double) v.getAsLong() / MULTIPLIER);
        } else {
            return OptionalDouble.empty();
        }
    }

    @Override
    public final Optional<BigDecimal> getDecimal(UUID uuid) {
        OptionalLong v = getRaw(uuid);
        if (v.isPresent()) {
            return Optional.of(BigDecimal.valueOf(v.getAsLong()).scaleByPowerOfTen(-FRACTIONAL_DIGITS));
        } else {
            return Optional.empty();
        }
    }

    protected abstract boolean set(UUID uuid, long balance);

    @Override
    public final boolean set(UUID uuid, double balance) {
        return set(uuid, double2long(balance));
    }

    @Override
    public final boolean set(UUID uuid, BigDecimal balance) {
        return set(uuid, decimal2long(balance));
    }

    private boolean has(UUID uuid, long amount) {
        OptionalLong balance = getRaw(uuid);
        if (!balance.isPresent()) {
            return false;
        }
        return balance.getAsLong() >= amount;
    }

    @Override
    public final boolean has(UUID uuid, double amount) {
        return has(uuid, (long) amount * MULTIPLIER);
    }

    @Override
    public final boolean has(UUID uuid, BigDecimal amount) {
        return has(uuid, amount.scaleByPowerOfTen(FRACTIONAL_DIGITS).longValue());
    }

    protected abstract boolean deposit(UUID uuid, long amount);

    @Override
    public final boolean deposit(UUID uuid, double amount) {
        return this.deposit(uuid, double2long(amount));
    }

    @Override
    public final boolean deposit(UUID uuid, BigDecimal amount) {
        return this.deposit(uuid, decimal2long(amount));
    }

    protected boolean withdraw(UUID uuid, long amount) {
        return this.deposit(uuid, -amount); // -n == +-n
    }

    @Override
    public final boolean withdraw(UUID uuid, double amount) {
        return this.withdraw(uuid, double2long(amount));
    }

    @Override
    public final boolean withdraw(UUID uuid, BigDecimal amount) {
        return this.withdraw(uuid, decimal2long(amount));
    }

    @Override
    public final boolean hasAccount(UUID uuid) {
        return getRaw(uuid).isPresent();
    }

    protected abstract boolean createAccount(UUID uuid, long balance);

    public final boolean createAccount(UUID uuid, double balance) {
        return createAccount(uuid, double2long(balance));
    }

    public final boolean createAccount(UUID uuid, BigDecimal balance) {
        return createAccount(uuid, decimal2long(balance));
    }

    @Override
    public final Map<UUID, BigDecimal> top(int limit, int offset) {
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

package jp.jyn.jecon.repository;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.db.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

public class LazyRepository extends AbstractRepository {
    private final Map<UUID, OptionalLong> balanceCache;

    public LazyRepository(MainConfig config, Database db) {
        super(config, db);
        this.balanceCache = new HashMap<>();
    }

    @Override
    public boolean save(UUID uuid) {
        OptionalLong balance = balanceCache.remove(uuid);
        if (balance != null && balance.isPresent()) {
            return db.setCreate(getId(uuid), balance.getAsLong());
        }
        return false;
    }

    @Override
    public void saveAll() {
        Jecon.getInstance().getLogger().info("Save balance");
        balanceCache.entrySet().removeIf(e -> {
            UUID uuid = e.getKey();
            OptionalLong balance = e.getValue();
            if (balance.isPresent()) {
                return db.setCreate(getId(uuid), balance.getAsLong());
            }
            return false;
        });
    }

    @Override
    protected OptionalLong getRaw(UUID uuid) {
        return balanceCache.computeIfAbsent(uuid, u -> db.getBalance(getId(u)));
    }

    @Override
    protected boolean set(UUID uuid, long balance) {
        if (hasAccount(uuid)) {
            balanceCache.put(uuid, OptionalLong.of(balance));
            return true;
        }
        return false;
    }

    @Override
    protected boolean deposit(UUID uuid, long amount) {
        OptionalLong balance = getRaw(uuid);
        if (!balance.isPresent()) {
            return false;
        }

        balanceCache.put(uuid, OptionalLong.of(balance.getAsLong() + amount));
        return true;
    }

    @Override
    protected boolean createAccount(UUID uuid, long balance) {
        if (hasAccount(uuid)) {
            return false;
        }
        balanceCache.put(uuid, OptionalLong.of(balance));
        return true;
    }

    @Override
    public boolean removeAccount(UUID uuid) {
        balanceCache.remove(uuid);
        return db.removeAccount(getId(uuid));
    }
}

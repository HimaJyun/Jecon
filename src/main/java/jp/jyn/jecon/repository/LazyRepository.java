package jp.jyn.jecon.repository;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.db.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;

public class LazyRepository extends AbstractRepository {
    private final Map<UUID, OptionalLong> dbBalance;
    private final Map<UUID, OptionalLong> balanceCache;

    public LazyRepository(MainConfig config, Database db) {
        super(config, db);
        this.dbBalance = new HashMap<>();
        this.balanceCache = new HashMap<>();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean sync(UUID uuid, OptionalLong balance) {
        // ここに来る時点でbalanceCacheは消された後なので、一貫性保持のためにdbBalanceも先に消す
        OptionalLong old = dbBalance.remove(uuid);

        if (balance == null || !balance.isPresent()) {
            // そもそもロードされてないか、アカウントがない
            return false;
        }

        if (old == null || !old.isPresent()) {
            // あり得ない。片方があるならこっちもあるはず。
            throw new IllegalStateException();
        }

        // 差を計算、差額分を反映
        long difference = balance.getAsLong() - old.getAsLong();
        if (difference == 0) {
            // 差がない(ログイン時とログアウト時の金額が同じ)なら何もしなくてもいい
            return false;
        }
        // UPDATEに失敗(アカウント非存在)でもアカウント作成はしない
        // 他のサーバーで行われた削除を確実に反映させるため、自分のサーバーでアカウントが作成された時はすでに書き込まれているはずなので
        return db.deposit(getId(uuid), difference);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public void consistency(UUID uuid) {
        // ログイン時に残高の一貫性を保つ仕組み
        balanceCache.computeIfPresent(uuid, (u, balance) -> {
            sync(u, balance);
            return null; // どちらにせよ値は消す(DBから最新の値を取得するために)
        });
        // これを定期的に回す方が良いかも
    }

    public void save(UUID uuid) {
        sync(uuid, balanceCache.remove(uuid));
    }

    public void saveAll() {
        Jecon.getInstance().getLogger().info("Save balance");
        balanceCache.entrySet().removeIf(e -> {
            sync(e.getKey(), e.getValue());
            return true; // 常に削除する
        });
    }

    @Override
    protected OptionalLong getRaw(UUID uuid) {
        return balanceCache.computeIfAbsent(uuid, u -> {
            OptionalLong current = db.getBalance(getId(u));
            dbBalance.put(u, current);
            return current;
        });
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
        dbBalance.put(uuid, OptionalLong.of(balance));
        // すぐに作成する
        // ここを遅延書き込みにすると保存処理をする際に(アカウントの非存在で作成……を行うために)必ずUPDATEが必要になる
        return db.createAccount(getId(uuid), balance);

        // 複数のサーバーにピッタリ"同時"にログインするとエラーになる可能性あり (複数サーバーでhasAccountがfalseに->create(INSERT)が複数走って一意制約違反)
        // とはいえ、これは"同時"にログインしないと起きないはずで、複数サーバーを実行する可能性のある状況(BungeeCord)ではログインするサーバーは常にどれか1つなのであり得ない (はず……)
    }

    @Override
    public boolean removeAccount(UUID uuid) {
        balanceCache.put(uuid, OptionalLong.empty());
        dbBalance.put(uuid, OptionalLong.empty());
        return db.removeAccount(getId(uuid));
    }
}

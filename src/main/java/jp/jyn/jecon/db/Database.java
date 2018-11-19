package jp.jyn.jecon.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.ConfigStruct;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 抽象的なデータベース操作を行うクラス
 *
 * @author HimaJyun
 */
public abstract class Database {

    // ====================
    // | ID | UUID | name |
    // ====================
    // | ID | balance |
    // ================

    public enum Reason {
        ACCOUNT_NOT_FOUND, NOT_ENOUGH, SUCCESS, UNKNOWN_ERROR,
    }

    // メインクラスのインスタンス
    protected Jecon jecon;
    // HikariCPのDataSource(注:要close();)
    private HikariDataSource hikariDc;

    // 設定構造体
    protected ConfigStruct config;

    // テーブル名の接頭辞、SQLiteの場合は常に空文字
    protected String prefix = "";

    // キャッシュ
    private final Map<UUID, Integer> uuidCache = new HashMap<>();
    private final Map<String, Integer> nameCache = new HashMap<>();
    private final Map<Integer, Double> balanceCache = new HashMap<>();

    protected void setup(Jecon jecon, HikariConfig hikariConfig) {
        this.jecon = jecon;
        if (config == null) {
            config = jecon.getConfigStruct();
        }
        this.prefix = config.getDbConfig().prefix;

        hikariConfig.setJdbcUrl(config.getDbConfig().url);
        hikariConfig.setAutoCommit(true);
        hikariConfig.setConnectionInitSql("/* Jecon */SELECT 1");

        if (config.getDbConfig().poolSize > 0) {
            hikariConfig.setMaximumPoolSize(config.getDbConfig().poolSize);
        }
        if (config.getDbConfig().timeout > 0) {
            hikariConfig.setIdleTimeout(config.getDbConfig().timeout);
        }

        hikariConfig.setDataSourceProperties(config.getDbConfig().propaties);

        // HikariDataSourceをインスタンス化
        this.hikariDc = new HikariDataSource(hikariConfig);

        this.createTable();
        // check db version
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `meta` (" +
                "   `key` TEXT," +
                "   `value` TEXT" +
                ")"
            );

            try (ResultSet resultSet = statement.executeQuery("SELECT `value` FROM `meta` WHERE key='dbversion'")) {
                if (resultSet.next()) {
                    if (!resultSet.getString("value").equals("1")) {
                        throw new RuntimeException("An incompatible change was made (database can not be downgraded)");
                    }
                } else {
                    statement.executeUpdate("INSERT INTO `meta` VALUES('dbversion','1')");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * データベース接続を終了します。
     */
    public void close() {
        if (hikariDc != null) {
            hikariDc.close();
        }
    }

    /**
     * テーブルを作成します。
     */
    protected abstract void createTable();

    /**
     * コネクションを取得します。
     *
     * @return 取得したSQLコネクション
     * @throws SQLException getConnection SQL error
     */
    protected Connection getConnection() throws SQLException {
        return hikariDc.getConnection();
    }

    // ======== getID ========

    /**
     * ユーザのIDを取得します。
     *
     * @param playerName 取得するプレイヤー名
     * @return 取得したID、なければ-1
     */
    private int getId(String playerName) {
        playerName = playerName.toLowerCase(Locale.ENGLISH);
        // キャッシュにあればそれを応答
        if (nameCache.containsKey(playerName)) {
            return nameCache.get(playerName);
        }
        // 無ければ問い合わせ
        int id = getId(playerName, false);
        // キャッシュに追加
        if (id != -1) {
            nameCache.put(playerName, id);
        }
        return id;
    }

    /**
     * ユーザのIDを取得します。
     *
     * @param player 取得するプレイヤー
     * @return 取得したID、なければ-1
     */
    private int getId(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        // キャッシュにあればそれを応答
        if (uuidCache.containsKey(uuid)) {
            return uuidCache.get(uuid);
        }
        // 無ければ問い合わせ
        int id = getId(uuid.toString(), true);
        // キャッシュに追加
        if (id != -1) {
            uuidCache.put(uuid, id);
        }
        return id;
    }

    /**
     * ユーザのIDをデータベースに問い合わせます、普段は利用しないで下さい。
     *
     * @param value       取得するアカウント名、もしくはUUID.toString()
     * @param valueIsUuid UUIDで検索するのであればtrue、名前であればfalse
     * @return 取得したID、なければ-1
     */
    private int getId(String value, boolean valueIsUuid) {
        // SELECT id FROM (prefix)account WHERE (uuid/name)=?

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "SELECT id FROM " + prefix + "account WHERE " + (valueIsUuid ? "uuid" : "name") + "=?"
             )) {
            // 値をセット
            prestat.setString(1, value.toLowerCase(Locale.ENGLISH));
            // 問い合わせ実行
            try (ResultSet rs = prestat.executeQuery()) {
                // 実行結果の取得
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ======== getName ========

    /**
     * 指定したIDから名前を取得します。
     *
     * @param id 名前を取得するID
     * @return 取得出来た名前、存在しなければnull
     */
    private String getName(int id) {
        List<Integer> list = new ArrayList<>();
        list.add(id);
        Map<Integer, String> map = getName(list);

        return map.getOrDefault(id, null);
    }

    /**
     * 複数のIDから名前を効率よく取得します。
     *
     * @param ids 取得するIDのリスト
     * @return 取得出来た名前が紐づけられたMap
     */
    private Map<Integer, String> getName(List<Integer> ids) {
        // SELECT name FROM (prefix)account WHERE id=?
        // 結果用変数
        Map<Integer, String> result = new HashMap<>();

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "SELECT name FROM " + prefix + "account WHERE id=?"
             )) {
            // ループ処理
            for (int id : ids) {
                if (id != -1) {
                    // 値をセット
                    prestat.setInt(1, id);
                    // 問い合わせ実行
                    try (ResultSet rs = prestat.executeQuery()) {
                        // 実行結果の取得
                        if (rs.next()) {
                            result.put(id, rs.getString(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // ======== hasAccount ========

    /**
     * アカウントが存在するかチェックします。
     *
     * @param playerName 確認するアカウント名
     * @return アカウントが存在すればtrue、そうでなければfalse
     */
    public boolean hasAccount(String playerName) {
        return hasAccount(getId(playerName));
    }

    /**
     * アカウントが存在するかチェックします。
     *
     * @param player 確認するアカウント
     * @return アカウントが存在すればtrue、そうでなければfalse
     */
    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(getId(player));
    }

    /**
     * 残高アカウントが存在するか確認します。
     *
     * @param id 確認するID
     * @return 結果
     */
    private boolean hasAccount(int id) {
        if (id == -1) {
            return false;
        }

        return getBalance(id) != -1;
    }

    // ======== getBalance ========

    /**
     * 指定したアカウントの残高を取得します。
     *
     * @param playerName 取得するアカウント名
     * @return 残高、アカウントが存在しない場合は-1
     */
    public double getBalance(String playerName) {
        return getBalance(getId(playerName));
    }

    /**
     * 指定したアカウントの残高を取得します。
     *
     * @param player 取得するアカウント
     * @return 残高、アカウントが存在しない場合は-1
     */
    public double getBalance(OfflinePlayer player) {
        // ID取得
        return getBalance(getId(player));
    }

    /**
     * 指定したアカウントの残高を取得します。
     *
     * @param id 確認するアカウントID
     * @return 残高、アカウントが存在しない場合は-1
     */
    private double getBalance(int id) {
        // アカウントが無い
        if (id == -1) {
            return -1;
        }
        // 残高照会(キャッシュから)
        if (balanceCache.containsKey(id)) {
            return balanceCache.get(id);
        }

        // 問い合わせ結果用変数
        double result = -1;
        // SELECT balance FROM (prefix)balance WHERE id=?

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "SELECT balance FROM " + prefix + "balance WHERE id=?"
             )) {
            // 値をセット
            prestat.setInt(1, id);
            // 問い合わせ実行
            try (ResultSet rs = prestat.executeQuery()) {
                // 実行結果の確認
                if (rs.next()) {
                    // あれば取得
                    result = rs.getDouble(1);
                    // キャッシュにプット
                    balanceCache.put(id, result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // ======== createPlayerAccount ========

    /**
     * 指定したプレイヤーのアカウントを作成します。
     *
     * @param playerName 作成するアカウント名
     * @return 作成に成功すればtrue、失敗すればfalse、既に存在する場合はtrue
     */
    @SuppressWarnings("deprecation")
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(jecon.getServer().getOfflinePlayer(playerName), config.getDefaultBalance());
    }

    /**
     * 指定したプレイヤーのアカウントを作成します。
     *
     * @param player 作成するアカウント
     * @return 作成に成功すればtrue、失敗すればfalse、既に存在する場合はtrue
     */
    public boolean createPlayerAccount(OfflinePlayer player) {
        return createPlayerAccount(player, config.getDefaultBalance());
    }

    /**
     * 指定したプレイヤーのアカウントを作成します。
     *
     * @param playerName 作成するアカウント名
     * @param amount     初期残高
     * @return 作成に成功すればtrue、失敗すればfalse、既に存在する場合はtrue
     */
    @SuppressWarnings("deprecation")
    public boolean createPlayerAccount(String playerName, double amount) {
        return createPlayerAccount(jecon.getServer().getOfflinePlayer(playerName), amount);
    }

    /**
     * 指定したプレイヤーのアカウントを作成します。
     *
     * @param player 作成するアカウント
     * @param amount 初期残高
     * @return 作成に成功すればtrue、失敗すればfalse、既に存在する場合はtrue
     */
    public boolean createPlayerAccount(OfflinePlayer player, double amount) {
        // IDの取得
        int id = getId(player);

        // 既にアカウントと残高が存在すれば名前の更新を行う
        if (hasAccount(id)) {
            nameUpdate(player);
            return true;
        }

        // 使う
        String name = player.getName().toLowerCase(Locale.ENGLISH);
        UUID uuid = player.getUniqueId();

        // 該当なし
        if (id == -1) {
            // INSERT INTO (prefix)account VALUES(null,?,?)

            // 接続
            try (Connection con = this.getConnection();
                 PreparedStatement prestat = con.prepareStatement(
                     "INSERT INTO " + prefix + "account VALUES(null,?,?)"
                 )) {
                // 値をセット
                prestat.setString(1, uuid.toString().toLowerCase(Locale.ENGLISH));
                prestat.setString(2, name);

                // 実行
                prestat.executeUpdate();

                id = getId(player);
                if (id != -1) {
                    // キャッシュ更新
                    uuidCache.put(uuid, id);
                    nameCache.put(name, id);
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 残高がなければ作成
        if (getBalance(id) == -1) {
            // 帰宅
            if (id == -1) {
                return false;
            }
            // INSERT INTO (prefix)balance VALUES(?,?)

            // 接続
            try (Connection con = this.getConnection();
                 PreparedStatement prestat = con.prepareStatement(
                     "INSERT INTO " + prefix + "balance VALUES(?,?)"
                 )) {
                // 値をセット
                prestat.setInt(1, id);
                prestat.setDouble(2, amount);

                // 実行
                prestat.executeUpdate();

                // キャッシュ追加
                balanceCache.put(id, amount);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // ======== removePlayerAccount ========

    /**
     * 指定したプレイヤーのアカウントを削除します。
     *
     * @param playerName 削除するアカウント名
     * @return 削除に成功すればtrue、失敗すればfalse
     */
    public Reason removePlayerAccount(String playerName) {
        return removePlayerAccount(getId(playerName));
    }

    /**
     * 指定したプレイヤーのアカウントを削除します。
     *
     * @param player 削除するアカウント
     * @return 削除に成功すればtrue、失敗すればfalse
     */
    public Reason removePlayerAccount(OfflinePlayer player) {
        return removePlayerAccount(getId(player));
    }

    /**
     * 指定したプレイヤーのアカウントを削除します。(IDを削除する意味はないため保持されます。)
     *
     * @param id 削除するプレイヤーのID
     * @return 削除に成功すればtrue、失敗すればfalse
     */
    private Reason removePlayerAccount(int id) {
        // 第一にアカウントが無いなら不要なので速攻リターン
        if (id == -1) {
            return Reason.ACCOUNT_NOT_FOUND;
        }

        // DELETE FROM (prefix)balance WHERE id=?

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "DELETE FROM " + prefix + "balance WHERE id=?"
             )) {
            // 値をセット
            prestat.setInt(1, id);

            // 実行
            prestat.executeUpdate();

            // キャッシュ削除
            balanceCache.remove(id);
            return Reason.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Reason.UNKNOWN_ERROR;
    }

    // ======== nameUpdate ========

    /**
     * 指定したアカウントのユーザ名を更新します。
     *
     * @param player 更新するユーザ
     */
    public void nameUpdate(OfflinePlayer player) {
        // ID確認
        int id = getId(player);
        // アカウントが無ければ抜ける
        if (id == -1) {
            return;
        }
        // 使う
        String name = player.getName().toLowerCase(Locale.ENGLISH);
        String oldName = getName(id);

        // UPDATE (prefix)account SET name=? WHERE id=?

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "UPDATE " + prefix + "account SET name=? WHERE id=?"
             )) {
            // 値をセット
            prestat.setString(1, name);
            prestat.setInt(2, id);

            // 実行
            prestat.executeUpdate();

            // キャッシュ更新
            if (oldName != null) {
                nameCache.remove(oldName);
            }
            nameCache.put(name, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======== has ========

    /**
     * 指定したプレイヤーの残高が指定した金額以上か確認します。
     *
     * @param playerName 確認するプレイヤー名
     * @param amount     確認する残高
     * @return 残高が足りていればtrue、不足していればfalse
     */
    public boolean has(String playerName, double amount) {
        return has(getBalance(playerName), amount);
    }

    /**
     * 指定したプレイヤーの残高が指定した金額以上か確認します。
     *
     * @param player 確認するプレイヤー
     * @param amount 確認する残高
     * @return 残高が足りていればtrue、不足していればfalse
     */
    public boolean has(OfflinePlayer player, double amount) {
        return has(getBalance(player), amount);
    }

    private boolean has(double balance, double amount) {
        return balance >= amount;
    }

    // ======== set ========

    /**
     * 指定したプレイヤーの残高をセットします。
     *
     * @param playerName セットするプレイヤー名
     * @param amount     セットする金額
     * @return 正常に終了すれば(アカウントが存在しなくても)true
     */
    public Reason set(String playerName, double amount) {
        return set(getId(playerName), amount);
    }

    /**
     * 指定したプレイヤーの残高をセットします。
     *
     * @param player セットするプレイヤー
     * @param amount セットする金額
     * @return 正常に終了すれば(アカウントが存在しなくても)true
     */
    public Reason set(OfflinePlayer player, double amount) {
        return set(getId(player), amount);
    }

    /**
     * 指定したプレイヤーの残高をセットします。
     *
     * @param id     セットするアカウントID
     * @param amount セットする金額
     * @return 正常に終了すれば(アカウントが存在しなくても)true
     */
    private Reason set(int id, double amount) {
        // アカウントが存在しなければ抜ける
        if (id == -1) {
            return Reason.ACCOUNT_NOT_FOUND;
        }

        // UPDATE (prefix)balance SET balance=? WHERE id=?;

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "UPDATE " + prefix + "balance SET balance=? WHERE id=?"
             )) {
            // 値をセット
            prestat.setDouble(1, amount);
            prestat.setInt(2, id);

            // 実行
            prestat.executeUpdate();

            // キャッシュ更新
            balanceCache.put(id, amount);
            return Reason.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Reason.UNKNOWN_ERROR;
    }

    // ======== depositPlayer ========

    /**
     * 指定したプレイヤーのアカウントに入金します。
     *
     * @param playerName 入金するアカウント名
     * @param amount     入金する金額
     * @return 入金結果
     */
    public Reason depositPlayer(String playerName, double amount) {
        return depositPlayer(getId(playerName), amount);
    }

    /**
     * 指定したプレイヤーのアカウントに入金します。
     *
     * @param player 入金するアカウント
     * @param amount 入金する金額
     * @return 入金結果
     */
    public Reason depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(getId(player), amount);
    }

    /**
     * 指定したプレイヤーのアカウントに入金します。
     *
     * @param id     確認するアカウントID
     * @param amount 入金する金額
     * @return 入金結果
     */
    private Reason depositPlayer(int id, double amount) {
        // アカウントが存在しなければ抜ける
        if (id == -1) {
            return Reason.ACCOUNT_NOT_FOUND;
        }
        double balance = getBalance(id);

        balance += amount;
        return set(id, balance);
    }

    // ======== withdrawPlayer ========

    /**
     * 指定したプレイヤーのアカウントに出金します。
     *
     * @param playerName 出金するアカウント名
     * @param amount     出金する金額
     * @return 出金結果
     */
    public Reason withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(getId(playerName), amount);
    }

    /**
     * 指定したプレイヤーのアカウントに出金します。
     *
     * @param player 出金するアカウント
     * @param amount 出金する金額
     * @return 出金結果
     */
    public Reason withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(getId(player), amount);
    }

    /**
     * 指定したプレイヤーのアカウントから出金します。
     *
     * @param id     確認するアカウントID
     * @param amount 出金する金額
     * @return 出金結果
     */
    private Reason withdrawPlayer(int id, double amount) {
        // アカウントが存在しなければ抜ける
        if (id == -1) {
            return Reason.ACCOUNT_NOT_FOUND;
        }
        double balance = getBalance(id);

        if (!has(balance, amount)) {
            // 残金が足りない
            return Reason.NOT_ENOUGH;
        }

        balance -= amount;
        return set(id, balance);
    }

    // ======== format ========

    /**
     * お金の表示を整形します。
     *
     * @param amount 整形元の金額
     * @return 整形後の金額
     */
    public String format(double amount) {
        // 123.45->123
        int tmpMajor = (int) amount;
        String major = NumberFormat.getNumberInstance().format(tmpMajor);
        StringBuilder all = new StringBuilder(major);

        // 123.45->0.45, 123.05->0.05, 123.1->0.1
        double i = amount - tmpMajor;
        // ～0.09->0
        if (i < 0.1) {
            i = 0;
        }
        // 0.45->length==4, 0.1->length==3
        // i * ( length==3->10, length==4 )
        int tmpMinor = (int) (i * (String.valueOf(i).length() == 3 ? 10 : 100));

        String minor = "";
        if (tmpMinor != 0) {
            minor = String.valueOf(tmpMinor);
            all.append(".");
            all.append(minor);
        }
        String currencyMinor = tmpMinor == 0 ? ""
            : (tmpMinor == 1 ? config.getFormatMinorSingle() : config.getFormatMinorPlural());
        String currencyMajor = tmpMajor > 1 ? config.getFormatMajorPlural() : config.getFormatMajorSingle();

        String result = config.getFormatFormat()
            .replace(ConfigStruct.FORMAT_MACRO_ALL, all.toString())
            .replace(ConfigStruct.FORMAT_MACRO_MAJOR, major)
            .replace(ConfigStruct.FORMAT_MACRO_MINOR, minor)
            .replace(ConfigStruct.FORMAT_MACRO_MAJOR_CURRENCY, currencyMajor)
            .replace(ConfigStruct.FORMAT_MACRO_MINOR_CURRENCY, currencyMinor);
        if (config.isFormatMergeSpace()) {
            result = result.replaceAll("  *", " ");
        }
        if (config.isFormatTrim()) {
            result = result.trim();
        }
        return result;
    }

    // ========= list =========

    /**
     * 金額のランキングを取得します。
     *
     * @param page ページ指定
     * @return 取得した結果の入ったEntryクラス
     */
    public List<Entry> topList(int page) {
        // 結果
        List<Entry> result = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        Map<Integer, Double> balance = new HashMap<>();

        // SELECT id,balance FROM (prefix)balance ORDER BY balance DESC LIMIT ?,?;

        // 接続
        try (Connection con = this.getConnection();
             PreparedStatement prestat = con.prepareStatement(
                 "SELECT id,balance FROM " + prefix + "balance ORDER BY balance DESC LIMIT ?,?"
             )) {
            // 値をセット
            prestat.setInt(1, (page - 1) * 10);
            prestat.setInt(2, config.getTopCommandEntryPerPage());
            // 実行
            try (ResultSet rs = prestat.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    ids.add(id);
                    balance.put(id, rs.getDouble(2));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<Integer, String> name = getName(ids);
        for (Integer id : ids) {
            if (name.containsKey(id)) {
                result.add(new Entry(name.get(id), balance.get(id)));
            }
        }
        return result;
    }

    /**
     * 個別エントリ用クラス
     *
     * @author HimaJyun
     */
    public class Entry {
        public final String user;
        public final double balance;

        Entry(String user, double balance) {
            this.user = user;
            this.balance = balance;
        }
    }
}

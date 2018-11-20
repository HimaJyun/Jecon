package jp.jyn.jecon.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Properties;

public class ConfigStruct {

    /*
     * 　　　■■■■■
     * 　　　　　　■
     * 　　　　　　■
     * 　　　　　　■
     * 　　　　　　■　　　　■■■　　　　　　■■■　■　　　　■■■　　　　■■　■■■
     * 　　　　　　■　　　■　　　■　　　　■　　　■■　　　■　　　■　　　　■■　　　■
     * 　　　　　　■　　■　　　　　■　　■　　　　　■　　■　　　　　■　　　■　　　　■
     * 　　　　　　■　　■■■■■■■　　■　　　　　　　　■　　　　　■　　　■　　　　■
     * ■　　　　　■　　■　　　　　　　　■　　　　　　　　■　　　　　■　　　■　　　　■
     * ■　　　　　■　　■　　　　　　　　■　　　　　　　　■　　　　　■　　　■　　　　■
     * ■　　　　　■　　■　　　　　■　　■　　　　　■　　■　　　　　■　　　■　　　　■
     * 　■　　　■　　　　■　　　　■　　　■　　　　■　　　■　　　■　　　　■　　　　■
     * 　　■■■　　　　　　■■■■　　　　　■■■■　　　　　■■■　　　　■■■　　■■■
     *
     * J = Hima[J]yun(and [J]ava...)
     * econ = [Econ]omy
     * (61万円1株売りを1円61万株売りにしそうな名前だけど気にしない……)
     */

    /**
     * 設定
     */
    private FileConfiguration conf = null;
    /**
     * カスタムコンフィグクラス
     */
    private final CustomConfig customconfig;
    /**
     * 使用されるプラグイン
     */
    private final Plugin plg;

    private boolean createAccountOnJoin = false;
    private double defaultBalance = 0;
    private int topCommandEntryPerPage = 10;

    private boolean formatTrim = false;
    private boolean formatMergeSpace = false;
    private String formatMajorSingle = "";
    private String formatMajorPlural = "";
    private String formatMinorSingle = "";
    private String formatMinorPlural = "";
    private String formatFormat = "";
    public static final String FORMAT_MACRO_MAJOR = "[%Major%]";
    public static final String FORMAT_MACRO_MINOR = "[%Minor%]";
    public static final String FORMAT_MACRO_ALL = "[%All%]";
    public static final String FORMAT_MACRO_MAJOR_CURRENCY = "[%MajorCurrency%]";
    public static final String FORMAT_MACRO_MINOR_CURRENCY = "[%MinorCurrency%]";

    private DbConfig dbConfig = null;

    /**
     * 各種設定構造体を初期化します。
     *
     * @param plugin 対象のプラグイン
     */
    public ConfigStruct(Plugin plugin) {
        // プラグイン
        plg = plugin;
        // カスタムコンフィグクラスをインスタンス化
        customconfig = new CustomConfig(plg);

        // 読み込み
        reloadConfig();
    }

    /**
     * 設定をリロードします。
     *
     * @return 自分自身のインスタンス
     */
    public ConfigStruct reloadConfig() {
        // デフォルトをセーブ
        customconfig.saveDefaultConfig();
        // confがnullではない(=リロード)
        if (conf != null) {
            customconfig.reloadConfig();
        }
        // 設定を取得
        conf = customconfig.getConfig();

        // バージョンがない->1.1.3未満
        if (!conf.contains("version", true)) {
            conf.set("version", 1);
            customconfig.saveConfig();
        }

        // ロード
        createAccountOnJoin = conf.getBoolean("CreateAccountOnJoin", false);
        defaultBalance = conf.getDouble("DefaultBalance", 0);
        topCommandEntryPerPage = conf.getInt("TopCommandEntryPerPage", 10);

        formatMergeSpace = conf.getBoolean("Format.MergeSpace", false);
        formatTrim = conf.getBoolean("Format.Trim", false);
        formatMajorSingle = customconfig.replaceColor(conf.getString("Format.Major.Single", ""));
        formatMajorPlural = customconfig.replaceColor(conf.getString("Format.Major.Plural", ""));
        formatMinorSingle = customconfig.replaceColor(conf.getString("Format.Minor.Single", ""));
        formatMinorPlural = customconfig.replaceColor(conf.getString("Format.Minor.Plural", ""));
        formatFormat = customconfig.replaceColor(conf.getString("Format.Format", ""));

        dbConfig = new DbConfig();

        return this;
    }

    /**
     * データベース設定
     *
     * @author HimaJyun
     */
    public final class DbConfig {
        public final boolean isMySQL;
        public final String url;
        public final String prefix;
        public final int poolSize;
        public final long timeout;
        public final Properties propaties = new Properties();

        private DbConfig() {
            String tmp = "jdbc:";
            if (conf.getString("Database.Type", "sqlite").equalsIgnoreCase("mysql")) {
                isMySQL = true;
                // mysql://localhost:3306/jecon
                tmp += "mysql://"
                       + conf.getString("Database.MySQL.Host", "localhost:3306")
                       + "/"
                       + conf.getString("Database.MySQL.Name", "jecon");
                propaties.put("user", conf.getString("Database.MySQL.User", "root"));
                propaties.put("password", conf.getString("Database.MySQL.Pass"));
                prefix = conf.getString("Database.MySQL.Prefix", "jecon_");
            } else {
                isMySQL = false;
                // sqlite:plugins/Jecon/jecon.db
                File tmpFile = new File(plg.getDataFolder(),
                    conf.getString("Database.SQLite.File", "jecon.db"));
                tmpFile.getParentFile().mkdirs();
                // URL
                tmp += "sqlite:" + tmpFile.getPath();
                prefix = "";
            }
            // 共通設定
            url = tmp;
            // プロパティ取得
            tmp = "Database." + (isMySQL ? "MySQL" : "SQLite") + ".Propaties";
            if (conf.contains(tmp)) {
                for (String key : conf.getConfigurationSection(tmp).getKeys(false)) {
                    propaties.put(key, conf.getString(tmp + "." + key));
                }
            }

            // パフォーマンス周り
            poolSize = conf.getInt("Database.Poolsize", -1);
            timeout = conf.getLong("Database.Timeout", -1);
        }
    }

    /**
     * ログイン時にアカウントを作成するか?
     *
     * @return trueなら作成、falseなら作成しない
     */
    public boolean isCreateAccountOnJoin() {
        return createAccountOnJoin;
    }

    /**
     * デフォルトの所持金
     *
     * @return デフォルトの所持金
     */
    public double getDefaultBalance() {
        return defaultBalance;
    }

    /**
     * "/money top"コマンド使用時に1ページに表示するエントリ数
     *
     * @return 1ページに表示するエントリ数
     */
    public int getTopCommandEntryPerPage() {
        return topCommandEntryPerPage;
    }

    /**
     * フォーマット後に文字列の前後の空白を削除するか
     *
     * @return trueなら削除、falseならそのまま
     */
    public boolean isFormatTrim() {
        return formatTrim;
    }

    /**
     * フォーマット後に生じる連続したスペースを削除するか
     *
     * @return trueなら削除、falseならそのまま
     */
    public boolean isFormatMergeSpace() {
        return formatMergeSpace;
    }

    /**
     * 整数金額の単数形表示単位
     *
     * @return 金額の単位
     */
    public String getFormatMajorSingle() {
        return formatMajorSingle;
    }

    /**
     * 整数金額の複数形表示単位
     *
     * @return 金額の単位
     */
    public String getFormatMajorPlural() {
        return formatMajorPlural;
    }

    /**
     * 小数金額の単数形表示単位
     *
     * @return 金額の単位
     */
    public String getFormatMinorSingle() {
        return formatMinorSingle;
    }

    /**
     * 小数金額の複数形表示単位
     *
     * @return 金額の単位
     */
    public String getFormatMinorPlural() {
        return formatMinorPlural;
    }

    /**
     * 実際に金額を表示する際に利用されるフォーマット
     *
     * @return フォーマット、マクロで置換して下さい。
     */
    public String getFormatFormat() {
        return formatFormat;
    }

    /**
     * データベース設定を取得
     *
     * @return データベース設定
     */
    public DbConfig getDbConfig() {
        return dbConfig;
    }
}

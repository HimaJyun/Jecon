package jp.jyn.jecon.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigStruct {

	/**
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

	private boolean isMysql = false;
	private String sqliteFilePath = "";
	private String mysqlHost = "localhost";
	private String mysqlPort = "3306";
	private String mysqlUser = "";
	private String mysqlPass = "";
	private String mysqlDb = "jecon";
	private String mysqlPrefix = "jecon_";
	private int mysqlPoolsize = 10;
	private long mysqlTimeout = 600000;

	/**
	 * 各種設定構造体を初期化します。
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
	 */
	public void reloadConfig() {
		// デフォルトをセーブ
		customconfig.saveDefaultConfig();
		// confがnullではない(=リロード)
		if (conf != null) {
			customconfig.reloadConfig();
		}
		// 設定を取得
		conf = customconfig.getConfig();

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

		if (conf.getString("Database.Type", "sqlite").equalsIgnoreCase("mysql")) {
			// MySQL
			isMysql = true;
			mysqlHost = conf.getString("Database.MySQL.Host", "localhost");
			mysqlPort = conf.getString("Database.MySQL.Port", "3306");
			mysqlUser = conf.getString("Database.MySQL.User", "root");
			mysqlPass = conf.getString("Database.MySQL.Pass", "pass");
			mysqlDb = conf.getString("Database.MySQL.DB", "jecon");
			mysqlPrefix = conf.getString("Database.MySQL.Prefix", "jecon_");
			mysqlPoolsize = conf.getInt("Database.MySQL.Poolsize", 10);
			mysqlTimeout = conf.getLong("Database.MySQL.Timeout", 600000);
		} else {
			// SQLite
			isMysql = false;
			sqliteFilePath = plg.getDataFolder() + File.separator + conf.getString("Database.SQLite.File", "jecon.db");
		}
	}

	/**
	 * ログイン時にアカウントを作成するか?
	 * @return trueなら作成、falseなら作成しない
	 */
	public boolean isCreateAccountOnJoin() {
		return createAccountOnJoin;
	}

	/**
	 * デフォルトの所持金
	 * @return デフォルトの所持金
	 */
	public double getDefaultBalance() {
		return defaultBalance;
	}

	/**
	 * "/money top"コマンド使用時に1ページに表示するエントリ数
	 * @return 1ページに表示するエントリ数
	 */
	public int getTopCommandEntryPerPage() {
		return topCommandEntryPerPage;
	}

	/**
	 * フォーマット後に文字列の前後の空白を削除するか
	 * @return trueなら削除、falseならそのまま
	 */
	public boolean isFormatTrim() {
		return formatTrim;
	}

	/**
	 * フォーマット後に生じる連続したスペースを削除するか
	 * @return trueなら削除、falseならそのまま
	 */
	public boolean isFormatMergeSpace() {
		return formatMergeSpace;
	}

	/**
	 * 整数金額の単数形表示単位
	 * @return 金額の単位
	 */
	public String getFormatMajorSingle() {
		return formatMajorSingle;
	}

	/**
	 * 整数金額の複数形表示単位
	 * @return 金額の単位
	 */
	public String getFormatMajorPlural() {
		return formatMajorPlural;
	}

	/**
	 * 小数金額の単数形表示単位
	 * @return 金額の単位
	 */
	public String getFormatMinorSingle() {
		return formatMinorSingle;
	}

	/**
	 * 小数金額の複数形表示単位
	 * @return 金額の単位
	 */
	public String getFormatMinorPlural() {
		return formatMinorPlural;
	}

	/**
	 * 実際に金額を表示する際に利用されるフォーマット
	 * @return フォーマット、マクロで置換して下さい。
	 */
	public String getFormatFormat() {
		return formatFormat;
	}

	/**
	 * 使用するデータベースの種類
	 * @return trueならMySQL、falseならSQLite
	 */
	public boolean isMysql() {
		return isMysql;
	}

	/**
	 * SQLite利用時のデータベースファイルのパス
	 * @return ファイルパス
	 */
	public String getSqliteFilePath() {
		return sqliteFilePath;
	}

	/**
	 * MySQL利用時のホスト名
	 * @return ホスト名
	 */
	public String getMysqlHost() {
		return mysqlHost;
	}

	/**
	 * MySQL利用時のポート番号
	 * @return ポート番号
	 */
	public String getMysqlPort() {
		return mysqlPort;
	}

	/**
	 * MySQL利用時のユーザ名
	 * @return ユーザ名
	 */
	public String getMysqlUser() {
		return mysqlUser;
	}

	/**
	 * MySQL利用時のパスワード
	 * @return パスワード
	 */
	public String getMysqlPass() {
		return mysqlPass;
	}

	/**
	 * MySQL利用時のデータベース名
	 * @return データベース名
	 */
	public String getMysqlDb() {
		return mysqlDb;
	}

	/**
	 * MySQL利用時のテーブルプレフィックス
	 * @return プレフィックス
	 */
	public String getMysqlPrefix() {
		return mysqlPrefix;
	}

	/**
	 * MySQL利用時のHikariCPプールサイズ
	 * @return プールサイズ
	 */
	public int getMysqlPoolsize() {
		return mysqlPoolsize;
	}

	/**
	 * MySQL利用時のタイムアウト
	 * @return タイムアウト
	 */
	public long getMysqlTimeout() {
		return mysqlTimeout;
	}
}
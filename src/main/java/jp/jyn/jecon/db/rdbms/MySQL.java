package jp.jyn.jecon.db.rdbms;

import java.sql.Connection;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.db.Database;

/**
 * MySQLに関する操作を行うクラス
 * @author HimaJyun
 *
 */
public class MySQL extends Database {

	public MySQL(Jecon jecon) {
		super.config = jecon.getConfigStruct();

		// プレフィックスを設定しておく
		super.prefix = super.config.getMysqlPrefix();
		HikariConfig config = new HikariConfig();

		config.setDriverClassName("com.mysql.jdbc.Driver");

		// jdbc:mysql://localhost:3306/jecon
		StringBuilder url = new StringBuilder("jdbc:mysql://");
		url.append(super.config.getMysqlHost());
		url.append(":");
		url.append(super.config.getMysqlPort());
		url.append("/");
		url.append(super.config.getMysqlDb());
		config.setJdbcUrl(url.toString());

		config.addDataSourceProperty("user", super.config.getMysqlUser());
		config.addDataSourceProperty("password", super.config.getMysqlPass());

		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");

		config.setMaximumPoolSize(super.config.getMysqlPoolsize());
		config.setIdleTimeout(super.config.getMysqlTimeout());

		super.setup(jecon, config);
	}

	/**
	 * テーブルを作成します。
	 */
	@Override
	protected void createTable() {
		// 接続を取得
		try (Connection con = super.getConnection();
				Statement stat = con.createStatement()) {
			// アカウントテーブル作成
			stat.executeUpdate(
					"CREATE TABLE IF NOT EXISTS " + super.prefix
							+ "account(id INT PRIMARY KEY AUTO_INCREMENT,uuid VARCHAR(36) UNIQUE,name VARCHAR(16),INDEX(name)) ENGINE=InnoDB");
			// 資産テーブル作成
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + super.prefix
					+ "balance(id INT PRIMARY KEY,balance DOUBLE) ENGINE=InnoDB");
		} catch (Exception e) {
			e.printStackTrace();
		}
		jecon.getLogger().info("MySQL enable.");
	}
}

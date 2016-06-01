package jp.jyn.jecon.db.rdbms;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.db.Database;

public class SQLite extends Database {

	public SQLite(Jecon jecon) {
		super.config = jecon.getConfigStruct();
		String dbPath = super.config.getSqliteFilePath();

		HikariConfig config = new HikariConfig();

		// サブディレクトリを作成
		(new File(dbPath.substring(0, dbPath.lastIndexOf(File.separator)))).mkdirs();

		config.setDriverClassName("org.sqlite.JDBC");
		config.setJdbcUrl("jdbc:sqlite:" + dbPath);
		config.setConnectionTestQuery("/* Jecon */SELECT 1");

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
					"CREATE TABLE IF NOT EXISTS account(id INTEGER PRIMARY KEY AUTOINCREMENT,uuid TEXT UNIQUE,name TEXT)");
			stat.executeUpdate("CREATE INDEX IF NOT EXISTS nameindex ON account(name)");
			// 残金テーブル作成
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS balance(id INTEGER PRIMARY KEY,balance REAL)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		jecon.getLogger().info("SQLite enable.");
	}

}

package jp.jyn.jecon.db.rdbms;

import com.zaxxer.hikari.HikariConfig;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.db.Database;

import java.sql.Connection;
import java.sql.Statement;

public class SQLite extends Database {

    public SQLite(Jecon jecon) {
        this.config = jecon.getConfigStruct();

        jecon.getLogger().info("Connect to SQLite.");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setConnectionTestQuery("/* Jecon */SELECT 1");

        super.setup(jecon, hikariConfig);
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

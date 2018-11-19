package jp.jyn.jecon.db.rdbms;

import com.zaxxer.hikari.HikariConfig;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.db.Database;

import java.sql.Connection;
import java.sql.Statement;

/**
 * MySQLに関する操作を行うクラス
 *
 * @author HimaJyun
 */
public class MySQL extends Database {

    public MySQL(Jecon jecon) {
        super.config = jecon.getConfigStruct();

        jecon.getLogger().info("Connect to MySQL.");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");

        // for Performance
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("alwaysSendSetIsolation", "false");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

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

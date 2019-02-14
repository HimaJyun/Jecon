package jp.jyn.jecon.db.driver;

import com.zaxxer.hikari.HikariDataSource;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.db.DBMigrationUtils;
import jp.jyn.jecon.db.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class MySQL extends Database {
    public MySQL(HikariDataSource hikari) {
        super(hikari);
    }

    @Override
    protected void createTable() {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `account` (" +
                    "`id`   INT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "`uuid` BINARY(16)    NOT NULL UNIQUE KEY" +
                    ")"
            );
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `balance` (" +
                    "`id`      INT    UNSIGNED NOT NULL  PRIMARY KEY," +
                    "`balance` BIGINT NOT NULL " +
                    ")"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void migration() {
        String version = DBMigrationUtils.getVersion(hikari);

        if (version.equals(DBMigrationUtils.CURRENT_VERSION)) {
            return;
        }

        Logger logger = Jecon.getInstance().getLogger();
        logger.info("Migrate MySQL");

        if (version.equals("1")) {
            v1to2(v1prefix());
        } else {
            logger.severe(DBMigrationUtils.MIGRATION_ERROR_1);
            logger.severe(DBMigrationUtils.MIGRATION_ERROR_2);
            throw new IllegalStateException(String.format(DBMigrationUtils.MIGRATION_EXCEPTION, version));
        }
    }

    private String v1prefix() {
        String prefix = System.getProperty("jecon.prefix");
        if (prefix != null) {
            return prefix;
        }

        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {
            while (resultSet.next()) {
                String table = resultSet.getString(1);
                if (table.endsWith("account")) {
                    if (prefix != null) {
                        Logger logger = Jecon.getInstance().getLogger();
                        logger.severe(DBMigrationUtils.MIGRATION_ERROR_1);
                        logger.severe("This database seems to be used by multiple Jecon.");
                        logger.severe("Since the prefix has been deleted, it is not possible to use one database with multiple Jecon.");
                        logger.severe("To continue processing, start up the server with -Djecon.prefix=<prefix>.");
                        throw new IllegalStateException("");
                    }
                    prefix = table.substring(0, table.length() - "account".length());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return prefix;
    }

    private void v1to2(String prefix) {
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("RENAME TABLE " +
                "`" + prefix + "account` TO `account_old`," +
                "`" + prefix + "balance` TO `balance_old`"
            );

            // data copy
            createTable();
            DBMigrationUtils.v1copy2(connection);

            // update version
            statement.executeUpdate("DROP TABLE `meta`");
            DBMigrationUtils.getVersion(hikari);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}

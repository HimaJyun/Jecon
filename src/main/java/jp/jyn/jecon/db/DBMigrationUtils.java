package jp.jyn.jecon.db;

import com.zaxxer.hikari.HikariDataSource;
import jp.jyn.jbukkitlib.uuid.UUIDBytes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DBMigrationUtils {
    private DBMigrationUtils() {}

    public final static String CURRENT_VERSION = "2";
    public final static String MIGRATION_ERROR_1 = "Unable to automatically migrate the database.";
    public final static String MIGRATION_ERROR_2 = "Check the document and check that the updating procedure is correct.";
    public final static String MIGRATION_EXCEPTION = "Unknown version(%s)";

    public static String getVersion(HikariDataSource hikari) {
        // check version
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `meta` (" +
                    "`key`   TEXT NOT NULL," +
                    "`value` TEXT NOT NULL" +
                    ")"
            );

            try (ResultSet resultSet = statement.executeQuery("SELECT `value` FROM `meta` WHERE `key`='dbversion'")) {
                if (resultSet.next()) {
                    return resultSet.getString("value");
                } else {
                    statement.executeUpdate("INSERT INTO `meta` VALUES('dbversion','" + CURRENT_VERSION + "')");
                    return CURRENT_VERSION;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void v1copy2(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);
            try (ResultSet resultSet = statement.executeQuery("SELECT `id`,`uuid` FROM `account_old`");
                 PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO `account` VALUES(?,?)"
                 )) {
                int i = 0;
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                    preparedStatement.setInt(1, id);
                    preparedStatement.setBytes(2, UUIDBytes.toBytes(uuid));
                    preparedStatement.addBatch();
                    i += 1;
                    if (i == 100) {
                        preparedStatement.executeBatch();
                        i = 0;
                    }
                }
                if (i != 0) {
                    preparedStatement.executeBatch();
                }
            }

            try (ResultSet resultSet = statement.executeQuery("SELECT `id`,`balance` FROM `balance_old`");
                 PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO `balance` VALUES (?,?)"
                 )) {
                int i = 0;
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    double balance = resultSet.getDouble("balance");
                    preparedStatement.setInt(1, id);
                    preparedStatement.setLong(2, (long) (balance * 100));
                    preparedStatement.addBatch();
                    i += 1;
                    if (i == 100) {
                        preparedStatement.executeBatch();
                        i = 0;
                    }
                }
                if (i != 0) {
                    preparedStatement.executeBatch();
                }
            }

            // drop temporary table
            statement.executeUpdate("DROP TABLE `account_old`");
            statement.executeUpdate("DROP TABLE `balance_old`");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}

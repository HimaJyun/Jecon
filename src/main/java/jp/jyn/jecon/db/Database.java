package jp.jyn.jecon.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jp.jyn.jbukkitlib.uuid.UUIDBytes;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.db.driver.MySQL;
import jp.jyn.jecon.db.driver.SQLite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class Database {
    protected final HikariDataSource hikari;

    protected Database(HikariDataSource hikari) {
        this.hikari = hikari;
    }

    public static Database connect(MainConfig.DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(config.url);
        hikariConfig.setPoolName("jecon-hikari");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setConnectionInitSql(config.init);
        hikariConfig.setDataSourceProperties(config.properties);

        if (config.maximumPoolSize > 0) {
            hikariConfig.setMaximumPoolSize(config.maximumPoolSize);
        }
        if (config.minimumIdle > 0) {
            hikariConfig.setMinimumIdle(config.minimumIdle);
        }
        if (config.maxLifetime > 0) {
            hikariConfig.setMaxLifetime(config.maxLifetime);
        }
        if (config.connectionTimeout > 0) {
            hikariConfig.setConnectionTimeout(config.connectionTimeout);
        }
        if (config.idleTimeout > 0) {
            hikariConfig.setIdleTimeout(config.idleTimeout);
        }

        Database database;
        Logger logger = Jecon.getInstance().getLogger();
        if (config.url.startsWith("jdbc:sqlite:")) {
            // SQLite
            logger.info("Use SQLite");
            database = new SQLite(new HikariDataSource(hikariConfig));
        } else if (config.url.startsWith("jdbc:mysql:")) {
            // MySQL
            logger.info("Use MySQL");
            hikariConfig.setUsername(config.username);
            hikariConfig.setPassword(config.password);
            database = new MySQL(new HikariDataSource(hikariConfig));
        } else {
            throw new IllegalArgumentException("Unknown jdbc");
        }

        database.migration();
        database.createTable();
        return database;
    }

    public void close() {
        if (hikari != null) {
            hikari.close();
        }
    }

    abstract protected void migration();

    abstract protected void createTable();

    public int getId(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                "SELECT `id` FROM `account` WHERE `uuid`=?"
            )) {
                byte[] byteUUID = UUIDBytes.toBytes(uuid);
                // get id
                statement.setBytes(1, byteUUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        connection.commit();
                        return resultSet.getInt(1);
                    }
                }

                // insert id
                try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO `account` (`uuid`) VALUES (?)"
                )) {
                    insert.setBytes(1, byteUUID);
                    insert.executeQuery();
                }

                // re get id
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        connection.commit();
                        return resultSet.getInt(1);
                    }
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("The ID could not be issued.");
    }

    public OptionalLong getBalance(int id) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT `balance` FROM `balance` WHERE `id`=?"
             )) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return OptionalLong.of(resultSet.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return OptionalLong.empty();
    }

    public boolean createAccount(int id, long balance) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO `balance` VALUES(?,?)"
             )) {
            statement.setInt(1, id);
            statement.setLong(2, balance);
            if (statement.executeUpdate() != 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean removeAccount(int id) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM `balance` WHERE `id`=?"
             )) {
            statement.setInt(1, id);
            return (statement.executeUpdate() != 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean setBalance(int id, long balance) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE `balance` SET `balance`=? WHERE `id`=?"
             )) {
            statement.setLong(1, balance);
            statement.setInt(2, id);
            return (statement.executeUpdate() != 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deposit(int id, long amount) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "UPDATE `balance` SET `balance`=`balance`+? WHERE `id`=?"
             )) {
            statement.setLong(1, amount);
            statement.setInt(2, id);
            return (statement.executeUpdate() != 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

package jp.jyn.jecon.config.migration;

import jp.jyn.jecon.Jecon;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class MainMigration {
    private final static String FILE = "config.yml";
    private final static int CURRENT_VERSION = 3;

    private MainMigration() {}

    public static boolean migration(ConfigurationSection config) {
        int version = config.getInt("version", -1);
        if (version == CURRENT_VERSION) {
            return false;
        }
        Logger logger = Jecon.getInstance().getLogger();
        logger.info("Migrate " + FILE);

        Path data = Jecon.getInstance().getDataFolder().toPath();
        try {
            Files.copy(data.resolve(FILE), data.resolve("config.old.yml"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        switch (version) {
            case 1:
                v1to2(config);
            case 2:
                v2to3(config);
                break;
            default:
                logger.severe(MigrationUtils.ERROR_1);
                logger.severe(String.format(MigrationUtils.ERROR_2, FILE));
                throw new IllegalStateException(String.format(MigrationUtils.EXCEPTION, FILE, version));
        }
        return true;
    }

    private static void v1to2(ConfigurationSection config) {
        config.set("cache.id", -1);
        config.set("cache.balance", -1);
        config.set("version", 2);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static void v2to3(ConfigurationSection config) {
        config.set("versionCheck", true);
        move(config, "DefaultBalance", "defaultBalance");
        move(config, "CreateAccountOnJoin", "createAccountOnJoin");
        config.set("TopCommandEntryPerPage", null);
        // "Format" can not be automatically migrated.
        config.set("Format", null);
        config.set("format.singularMajorSingularMinor", "{major} dollar {minor} cent");
        config.set("format.singularMajorPluralMinor", "{major} dollar {minor} cents");
        config.set("format.pluralMajorSingularMinor", "{major} dollars {minor} cent");
        config.set("format.pluralMajorPluralMinor", "{major} dollars {minor} cents");
        config.set("format.singleMajorZeroMinor", "{major} dollar");
        config.set("format.pluralMajorZeroMinor", "{major} dollars");
        // database
        move(config, "Database.Type", "database.type");
        move(config, "Database.SQLite.File", "database.sqlite.file");
        move(config, "Database.MySQL.Host", "database.mysql.host");
        move(config, "Database.MySQL.Name", "database.mysql.name");
        move(config, "Database.MySQL.User", "database.mysql.username");
        move(config, "Database.MySQL.Pass", "database.mysql.password");
        config.set("Database.MySQL.Prefix", null);
        config.set("database.mysql.init", "SET SESSION query_cache_type=0");
        v2DBProperties(config);
        // pool
        move(config, "Database.Poolsize", "database.connectionPool.maximumPoolSize");
        config.set("database.connectionPool.minimumIdle", -1);
        config.set("database.connectionPool.maxLifetime", -1);
        config.set("database.connectionPool.connectionTimeout", -1);
        move(config, "Database.Timeout", "database.connectionPool.idleTimeout");
        config.set("Database", null);

        config.set("version", 3);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static void v2DBProperties(ConfigurationSection config) {
        config.set("database.mysql.properties.useSSL", "false");
        config.set("database.mysql.properties.maintainTimeStates", "false");
        config.set("database.mysql.properties.elideSetAutoCommits", "true");
        config.set("database.mysql.properties.useLocalSessionState", "true");
        config.set("database.mysql.properties.alwaysSendSetIsolation", "false");
        config.set("database.mysql.properties.cacheServerConfiguration", "true");
        config.set("database.mysql.properties.cachePrepStmts", "true");
        config.set("database.mysql.properties.prepStmtCacheSize", "250");
        config.set("database.mysql.properties.prepStmtCacheSqlLimit", "2048");
        if (config.contains("Database.MySQL.Propaties")) {
            for (String key : config.getConfigurationSection("Database.MySQL.Propaties").getKeys(false)) {
                config.set("database.mysql.properties." + key, config.getString("Database.MySQL.Propaties." + key));
            }
        }
    }

    private static void move(ConfigurationSection config, String oldKey, String newKey) {
        MigrationUtils.move(config, oldKey, newKey);
    }
}

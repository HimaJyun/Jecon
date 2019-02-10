package jp.jyn.jecon.config;

import jp.jyn.jbukkitlib.cache.CacheFactory;
import jp.jyn.jbukkitlib.config.parser.template.StringParser;
import jp.jyn.jbukkitlib.config.parser.template.TemplateParser;
import jp.jyn.jbukkitlib.util.PackagePrivate;
import jp.jyn.jecon.Jecon;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Properties;

public class MainConfig {
    public final boolean versionCheck;

    public final BigDecimal defaultBalance;
    public final boolean createAccountOnJoin;

    public final FormatConfig format;
    public final DatabaseConfig database;
    public final CacheConfig cache;

    @PackagePrivate
    MainConfig(ConfigurationSection config) {
        versionCheck = config.getBoolean("versionCheck");
        defaultBalance = BigDecimal.valueOf(config.getDouble("defaultBalance"));
        createAccountOnJoin = config.getBoolean("createAccountOnJoin");

        format = new FormatConfig(config.getConfigurationSection("format"));
        database = new DatabaseConfig(config.getConfigurationSection("database"));
        cache = new CacheConfig(config.getConfigurationSection("cache"));
    }

    public final static class FormatConfig {
        public final TemplateParser singularMajorSingularMinor;
        public final TemplateParser singularMajorPluralMinor;
        public final TemplateParser pluralMajorSingularMinor;
        public final TemplateParser pluralMajorPluralMinor;
        public final TemplateParser singleMajorZeroMinor;
        public final TemplateParser pluralMajorZeroMinor;

        private FormatConfig(ConfigurationSection config) {
            singularMajorSingularMinor = StringParser.parse(config.getString("singularMajorSingularMinor"));
            singularMajorPluralMinor = StringParser.parse(config.getString("singularMajorPluralMinor"));
            pluralMajorSingularMinor = StringParser.parse(config.getString("pluralMajorSingularMinor"));
            pluralMajorPluralMinor = StringParser.parse(config.getString("pluralMajorPluralMinor"));
            singleMajorZeroMinor = StringParser.parse(config.getString("singleMajorZeroMinor"));
            pluralMajorZeroMinor = StringParser.parse(config.getString("pluralMajorZeroMinor"));
        }
    }

    public final static class DatabaseConfig {
        public final String url;
        public final String username;
        public final String password;
        public final String init;
        public final Properties properties = new Properties();

        public final int maximumPoolSize;
        public final int minimumIdle;
        public final long maxLifetime;
        public final long connectionTimeout;
        public final long idleTimeout;

        private DatabaseConfig(ConfigurationSection config) {
            String type = config.getString("type", "").toLowerCase(Locale.ENGLISH);
            if (type.equals("sqlite")) {
                File file = new File(Jecon.getInstance().getDataFolder(), config.getString("sqlite.file"));
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                url = "jdbc:sqlite:" + file.getPath();
                username = null;
                password = null;
            } else if (type.equals("mysql")) {
                url = String.format("jdbc:mysql://%s/%s", config.getString("mysql.host"), config.getString("mysql.name"));
                username = config.getString("mysql.username");
                password = config.getString("mysql.password");
            } else {
                throw new IllegalArgumentException("Invalid value: database.type(config.yml)");
            }
            init = config.getString(type + ".init", "/* Jecon */SELECT 1");
            String tmp = type + ".properties";
            if (config.contains(tmp)) {
                for (String key : config.getConfigurationSection(tmp).getKeys(false)) {
                    properties.put(key, config.getString(tmp + "." + key));
                }
            }

            maximumPoolSize = config.getInt("database.connectionPool.maximumPoolSize");
            minimumIdle = config.getInt("database.connectionPool.minimumIdle");
            maxLifetime = config.getLong("database.connectionPool.maxLifetime");
            connectionTimeout = config.getLong("database.connectionPool.connectionTimeout");
            idleTimeout = config.getLong("database.connectionPool.idleTimeout");
        }
    }

    public final static class CacheConfig {
        public final CacheFactory id;
        public final CacheFactory balance;

        private CacheConfig(ConfigurationSection config) {
            id = new CacheFactory.Sized(config.getInt("id"));
            balance = new CacheFactory.Sized(config.getInt("balance"));
        }
    }
}

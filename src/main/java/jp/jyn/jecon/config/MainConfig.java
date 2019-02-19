package jp.jyn.jecon.config;

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

    public final boolean lazyWrite;
    public final FormatConfig format;
    public final DatabaseConfig database;

    @PackagePrivate
    MainConfig(ConfigurationSection config) {
        versionCheck = config.getBoolean("versionCheck");
        defaultBalance = new BigDecimal(config.getString("defaultBalance"));
        createAccountOnJoin = config.getBoolean("createAccountOnJoin");

        lazyWrite = config.getBoolean("lazyWrite");

        format = new FormatConfig(config.getConfigurationSection("format"));
        database = new DatabaseConfig(config.getConfigurationSection("database"));
    }

    public final static class FormatConfig {
        public final String singularMajor;
        public final String pluralMajor;
        public final String singularMinor;
        public final String pluralMinor;
        public final TemplateParser format;
        public final TemplateParser formatZeroMinor;

        private FormatConfig(ConfigurationSection config) {
            singularMajor = config.getString("singularMajor");
            pluralMajor = config.getString("pluralMajor");
            singularMinor = config.getString("singularMinor");
            pluralMinor = config.getString("pluralMinor");
            format = StringParser.parse(config.getString("format"));
            if (config.contains("formatZeroMinor")) {
                formatZeroMinor = StringParser.parse(config.getString("formatZeroMinor"));
            } else {
                formatZeroMinor = format;
            }
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

            maximumPoolSize = config.getInt("connectionPool.maximumPoolSize");
            minimumIdle = config.getInt("connectionPool.minimumIdle");
            maxLifetime = config.getLong("connectionPool.maxLifetime");
            connectionTimeout = config.getLong("connectionPool.connectionTimeout");
            idleTimeout = config.getLong("connectionPool.idleTimeout");
        }
    }
}

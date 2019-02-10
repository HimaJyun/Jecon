package jp.jyn.jecon.config.migration;

import jp.jyn.jbukkitlib.util.PackagePrivate;
import org.bukkit.configuration.ConfigurationSection;

@PackagePrivate
class MigrationUtils {
    private MigrationUtils() {}

    @PackagePrivate
    final static String ERROR_1 = "Unable to automatically migrate the settings.";
    @PackagePrivate
    final static String ERROR_2 = "Delete %s and reload it.";
    @PackagePrivate
    final static String EXCEPTION = "Unknown version(%s:%d)";

    @PackagePrivate
    static void move(ConfigurationSection config, String oldKey, String newKey) {
        Object old = config.get(oldKey);
        config.set(oldKey, null);
        config.set(newKey, old);
    }
}

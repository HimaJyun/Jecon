package jp.jyn.jecon.config;

import jp.jyn.jbukkitlib.config.YamlLoader;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.migration.MainMigration;
import org.bukkit.plugin.Plugin;

public class ConfigLoader {
    private final YamlLoader mainLoader;
    private MainConfig mainConfig;

    public ConfigLoader() {
        Plugin plugin = Jecon.getInstance();
        this.mainLoader = new YamlLoader(plugin, "config.yml");
    }

    public void reloadConfig() {
        mainLoader.saveDefaultConfig();
        if (mainConfig != null) {
            mainLoader.reloadConfig();
        }

        if (MainMigration.migration(mainLoader.getConfig())) {
            mainLoader.saveConfig();
        }

        mainConfig = new MainConfig(mainLoader.getConfig());
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }
}

package jp.jyn.jecon.config;

import jp.jyn.jbukkitlib.config.YamlLoader;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.migration.MainMigration;
import jp.jyn.jecon.config.migration.MessageMigration;
import org.bukkit.plugin.Plugin;

public class ConfigLoader {
    private final YamlLoader mainLoader;
    private MainConfig mainConfig;

    private final YamlLoader messageLoader;
    private MessageConfig messageConfig;

    public ConfigLoader() {
        Plugin plugin = Jecon.getInstance();
        this.mainLoader = new YamlLoader(plugin, "config.yml");
        this.messageLoader = new YamlLoader(plugin, "message.yml");
    }

    public void reloadConfig() {
        mainLoader.saveDefaultConfig();
        messageLoader.saveDefaultConfig();
        if (mainConfig != null || messageConfig != null) {
            mainLoader.reloadConfig();
            messageLoader.reloadConfig();
        }

        if (MainMigration.migration(mainLoader.getConfig())) {
            mainLoader.saveConfig();
        }
        if (MessageMigration.migration(messageLoader.getConfig())) {
            messageLoader.saveConfig();
        }

        mainConfig = new MainConfig(mainLoader.getConfig());
        messageConfig = new MessageConfig(messageLoader.getConfig());
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
}

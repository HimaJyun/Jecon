package jp.jyn.jecon.config.migration;

import jp.jyn.jecon.Jecon;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

public class MessageMigration {
    private final static String FILE = "message.yml";
    private final static int CURRENT_VERSION = 2;

    private MessageMigration() {}

    @SuppressWarnings("Duplicates")
    public static boolean migration(ConfigurationSection config) {
        int version = config.getInt("version", -1);
        if (version == CURRENT_VERSION) {
            return false;
        }
        Logger logger = Jecon.getInstance().getLogger();
        logger.info("Migrate " + FILE);

        MigrationUtils.copy(FILE, "message.old.yml");

        if (version == 1) {
            v1to2(config);
        } else {
            logger.severe(MigrationUtils.ERROR_1);
            logger.severe(String.format(MigrationUtils.ERROR_2, FILE));
            throw new IllegalStateException(String.format(MigrationUtils.EXCEPTION, FILE, version));
        }
        return true;
    }

    private static void v1to2(ConfigurationSection config) {
        final String[] m = {"[%player%]", "{name}", "[%balance%]", "{balance}"};

        move(config, "DontHavePermission", "doNotHavePermission");
        config.set("missingArgument", "&cArgument is missing.");
        config.set("invalidArgument", "&cInvalid argument&r: {value}");
        config.set("playerNotFound", "&cPlayer not found&r: {name}");
        move(config, "AccountNotFound", "accountNotFound");
        config.set("notEnough", "&cThe balance of the account is not enough.");
        config.set("InvalidAmount", null);
        config.set("UnknownError", null);

        move(config, "Show.Success", "show");
        macro(config, "show", m);
        config.set("Show", null);

        move(config, "Pay.Self", "paySelf");
        move(config, "Pay.Success", "paySuccess");
        move(config, "Pay.Receive", "payReceive");
        move(config, "Set.Success", "set");
        move(config, "Give.Success", "give");
        move(config, "Take.Success", "take");
        move(config, "Create.Success", "create");
        move(config, "Create.Exists", "createAlready");
        move(config, "Remove.Success", "remove");
        macro(config, "paySuccess", m);
        macro(config, "payReceive", m);
        macro(config, "set", m);
        macro(config, "give", m);
        macro(config, "take", m);
        macro(config, "create", m);
        macro(config, "createAlready", m);
        macro(config, "remove", m);
        config.set("Pay", null);
        config.set("Set", null);
        config.set("Give", null);
        config.set("Take", null);
        config.set("Create", null);
        config.set("Remove", null);

        config.set("reloaded", "Config has been reloaded.");

        move(config, "Top.First", "topFirst");
        move(config, "Top.Entry", "topEntry");
        macro(config, "topFirst", "[%page%]", "{page}");
        macro(config, "topEntry", m);
        macro(config, "topEntry", "[%rank%]", "{rank}");
        config.set("Top", null);

        config.set("newVersion", new String[]{"New version available: {old} -> {new}", "Download: {url}"});

        move(config, "Help.Show", "help.show");
        move(config, "Help.Pay", "help.pay");
        move(config, "Help.Set", "help.set");
        move(config, "Help.Give", "help.give");
        move(config, "Help.Take", "help.take");
        move(config, "Help.Create", "help.create");
        move(config, "Help.Remove", "help.remove");
        move(config, "Help.Top", "help.top");
        move(config, "Help.Reload", "help.reload");
        config.set("help.version", "Show version.");
        move(config, "Help.Help", "help.help");
        config.set("Help", null);

        config.set("version", 2);
    }

    private static void move(ConfigurationSection config, String oldKey, String newKey) {
        MigrationUtils.move(config, oldKey, newKey);
    }

    private static void macro(ConfigurationSection config, String key, String... replace) {
        String str = config.getString(key);
        for (int i = 0; i < replace.length; i += 2) {
            str = str.replace(replace[i], replace[i + 1]);
        }
        config.set(key, str);
    }
}

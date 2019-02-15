package jp.jyn.jecon.config;

import jp.jyn.jbukkitlib.config.parser.template.StringParser;
import jp.jyn.jbukkitlib.config.parser.template.TemplateParser;
import jp.jyn.jbukkitlib.util.PackagePrivate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MessageConfig {
    private final static String PREFIX = "[Jecon] ";
    public final static String HEADER = "========== Jecon ==========";
    public final static String PLAYER_ONLY = PREFIX + ChatColor.RED + "This command can only be run by players.";

    public final TemplateParser doNotHavePermission;
    public final TemplateParser missingArgument;
    /**
     * value
     */
    public final TemplateParser invalidArgument;
    /**
     * name
     */
    public final TemplateParser playerNotFound;

    /**
     * name
     */
    public final TemplateParser accountNotFound;
    public final TemplateParser notEnough;

    /**
     * name,balance
     */
    public final TemplateParser show;
    public final TemplateParser paySelf;
    /**
     * amount,name
     */
    public final TemplateParser paySuccess;
    /**
     * amount,name
     */
    public final TemplateParser payReceive;
    /**
     * name,balance
     */
    public final TemplateParser set;
    /**
     * amount,name
     */
    public final TemplateParser give;
    /**
     * amount,name
     */
    public final TemplateParser take;
    /**
     * name,balance
     */
    public final TemplateParser create;
    /**
     * name
     */
    public final TemplateParser createAlready;
    /**
     * name,balance
     */
    public final TemplateParser remove;
    public final TemplateParser reloaded;

    /**
     * page
     */
    public final TemplateParser topFirst;
    /**
     * rank,name,balance
     */
    public final TemplateParser topEntry;

    /**
     * old,new,url
     */
    public final List<TemplateParser> newVersion;

    public final HelpMessage help;

    @PackagePrivate
    MessageConfig(ConfigurationSection config) {
        doNotHavePermission = parse(config, "doNotHavePermission");
        missingArgument = parse(config, "missingArgument");
        invalidArgument = parse(config, "invalidArgument");
        playerNotFound = parse(config, "playerNotFound");

        accountNotFound = parse(config, "accountNotFound");
        notEnough = parse(config, "notEnough");

        show = parse(config, "show");
        paySelf = parse(config, "paySelf");
        paySuccess = parse(config, "paySuccess");
        payReceive = parse(config, "payReceive");
        set = parse(config, "set");
        give = parse(config, "give");
        take = parse(config, "take");
        create = parse(config, "create");
        createAlready = parse(config, "createAlready");
        remove = parse(config, "remove");
        reloaded = parse(config, "reloaded");

        topFirst = parse(config.getString("topFirst"));
        topEntry = parse(config.getString("topEntry"));

        newVersion = config.getStringList("newVersion")
            .stream()
            .map(MessageConfig::parse)
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        help = new HelpMessage(config.getConfigurationSection("help"));
    }

    public final static class HelpMessage {
        public final TemplateParser show;
        public final TemplateParser pay;
        public final TemplateParser set;
        public final TemplateParser give;
        public final TemplateParser take;
        public final TemplateParser create;
        public final TemplateParser remove;
        public final TemplateParser top;
        public final TemplateParser reload;
        public final TemplateParser version;
        public final TemplateParser help;
        public final TemplateParser example;

        private HelpMessage(ConfigurationSection config) {
            show = parse(config.getString("show"));
            pay = parse(config.getString("pay"));
            set = parse(config.getString("set"));
            give = parse(config.getString("give"));
            take = parse(config.getString("take"));
            create = parse(config.getString("create"));
            remove = parse(config.getString("remove"));
            top = parse(config.getString("top"));
            reload = parse(config.getString("reload"));
            version = parse(config.getString("version"));
            help = parse(config.getString("help"));
            example = parse(config.getString("example"));
        }
    }

    private static TemplateParser parse(ConfigurationSection config, String key) {
        return parse(PREFIX + config.getString(key));
    }

    private static TemplateParser parse(String value) {
        return StringParser.parse(value);
    }
}

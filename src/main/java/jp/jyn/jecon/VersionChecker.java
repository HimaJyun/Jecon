package jp.jyn.jecon;

import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.util.updater.GitHubReleaseChecker;
import jp.jyn.jbukkitlib.util.updater.UpdateChecker;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class VersionChecker {
    private final static long CHECK_PERIOD = TimeUnit.HOURS.toMillis(12);

    private final boolean enable;
    private final MessageConfig message;
    private final UpdateChecker checker = new GitHubReleaseChecker("HimaJyun", "Jecon");

    private long nextCheck = 0;
    private String[] result = null;

    public VersionChecker(boolean enable, MessageConfig message) {
        this.enable = enable;
        this.message = message;
    }

    public void check(CommandSender sender) {
        if (!enable) {
            return;
        }

        if (nextCheck > System.currentTimeMillis()) {
            if (result != null) {
                sender.sendMessage(result);
            }
            return;
        }

        Plugin plugin = Jecon.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            nextCheck = CHECK_PERIOD + System.currentTimeMillis();
            UpdateChecker.LatestVersion latest = checker.callEx();

            String currentVersion = plugin.getDescription().getVersion();
            if (currentVersion.equals(latest.version)) {
                return;
            }

            TemplateVariable variable = StringVariable.init()
                .put("old", currentVersion)
                .put("new", latest.version)
                .put("url", latest.url);
            result = Stream.concat(
                Stream.of(MessageConfig.HEADER),
                message.newVersion.stream().map(parser -> parser.toString(variable))
            ).toArray(String[]::new);

            sender.sendMessage(result);
        });
    }
}

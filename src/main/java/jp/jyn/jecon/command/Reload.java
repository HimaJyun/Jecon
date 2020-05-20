package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Queue;

public class Reload extends SubCommand {
    private final MessageConfig message;

    public Reload(MessageConfig message) {
        this.message = message;
    }

    @Override
    protected Result onCommand(CommandSender sender, Queue<String> args) {
        Plugin plugin = Jecon.getInstance();
        plugin.getServer().getPluginManager().callEvent(new PluginDisableEvent(plugin));
        plugin.onDisable();
        plugin.onEnable();
        plugin.getServer().getPluginManager().callEvent(new PluginEnableEvent(plugin));

        sender.sendMessage(message.reloaded.toString());
        if (sender instanceof Player) {
            Bukkit.getConsoleSender().sendMessage(message.reloaded.toString());
        }

        return Result.OK;
    }

    @Override
    protected String requirePermission() {
        return "jecon.reload";
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money reload",
            message.help.reload.toString()
        );
    }
}

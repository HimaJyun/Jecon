package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.ErrorExecutor;
import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class Help extends SubCommand implements ErrorExecutor {
    private final MessageConfig message;
    private final Map<String, SubCommand> commands;

    public Help(MessageConfig message, Map<String, SubCommand> commands) {
        this.message = message;
        this.commands = commands;
    }


    @Override
    public boolean onError(Info error) {
        CommandSender sender = error.sender;
        switch (error.cause) {
            case ERROR:
                sendSubDetails(sender, error.subCommand);
                break;
            case COMMAND_NOT_FOUND:
                sendSubCommands(sender);
                break;
            case DONT_HAVE_PERMISSION:
                sender.sendMessage(message.doNotHavePermission.toString());
                break;
            case MISSING_ARGUMENT:
                sender.sendMessage(message.missingArgument.toString());
                sendSubDetails(sender, error.subCommand);
                break;
            case PLAYER_ONLY:
                sender.sendMessage(MessageConfig.PLAYER_ONLY);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Error: " + ChatColor.RESET + error.cause.name());
                break;
        }
        return true;
    }

    @Override
    protected Result execCommand(CommandSender sender, Queue<String> args) {
        // search detail help.
        SubCommand cmd = null;
        if (!args.isEmpty()) {
            cmd = commands.get(
                args.remove().toLowerCase(Locale.ENGLISH)
            );
        }

        if (cmd == null) {
            sendSubCommands(sender);
        } else {
            sendSubDetails(sender, cmd);
        }
        return Result.OK;
    }

    @Override
    protected List<String> execTabComplete(CommandSender sender, Deque<String> args) {
        if (args.size() == 1) {
            return commands.keySet().stream()
                .filter(str -> str.startsWith(args.getFirst()))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money help [command]",
            message.help.help.toString(),
            "/money help",
            "/money help show"
        );
    }

    private void sendSubCommands(CommandSender sender) {
        String[] messages = commands.values()
            .stream()
            .map(SubCommand::getHelp)
            .filter(Objects::nonNull)
            .map(help -> help.usage + " - " + help.description)
            .toArray(String[]::new);

        sender.sendMessage(MessageConfig.HEADER);
        sender.sendMessage(messages);
    }

    private void sendSubDetails(CommandSender sender, SubCommand cmd) {
        CommandHelp help = cmd.getHelp();
        if (help == null) {
            return;
        }

        sender.sendMessage(MessageConfig.HEADER);
        sender.sendMessage(help.usage);
        sender.sendMessage(help.description);
        if (help.example.length != 0) {
            sender.sendMessage("");
            sender.sendMessage(message.help.example.toString());
            for (String ex : help.example) {
                sender.sendMessage(ex);
            }
        }
    }
}

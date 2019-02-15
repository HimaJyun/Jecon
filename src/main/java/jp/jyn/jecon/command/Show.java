package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.BalanceRepository;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class Show extends SubCommand {
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Show(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @Override
    protected Result execCommand(Player sender, Queue<String> args) {
        if (!args.isEmpty()) {
            return execCommand((CommandSender) sender, args);
        }
        if (!sender.hasPermission("jecon.show")) {
            return Result.DONT_HAVE_PERMISSION;
        }

        sender.sendMessage(format(sender.getUniqueId(), StringVariable.init().put("name", sender.getName())));
        return Result.OK;
    }

    @Override
    protected Result execCommand(CommandSender sender, Queue<String> args) {
        if (!sender.hasPermission("jecon.show.other")) {
            return Result.DONT_HAVE_PERMISSION;
        }
        if (args.isEmpty()) {
            return Result.MISSING_ARGUMENT;
        }

        registry.getUUIDAsync(args.element()).thenAcceptSync(uuid -> {
            StringVariable variable = StringVariable.init().put("name", args.element());
            if (!uuid.isPresent()) {
                sender.sendMessage(message.playerNotFound.toString(variable));
                return;
            }

            sender.sendMessage(format(uuid.get(), variable));
        });
        return Result.OK;
    }

    @Override
    protected List<String> execTabComplete(CommandSender sender, Deque<String> args) {
        return CommandUtils.tabCompletePlayer(args);
    }

    private String format(UUID uuid, TemplateVariable variable) {
        return repository.format(uuid)
            .map(f -> message.show.toString(variable.put("balance", f)))
            .orElseGet(() -> message.accountNotFound.toString(variable));
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money show [player]",
            message.help.show.toString(),
            "/money show",
            "/money show notch"
        );
    }
}

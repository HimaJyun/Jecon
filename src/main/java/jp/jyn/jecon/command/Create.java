package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.BalanceRepository;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public class Create extends SubCommand {
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Create(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @Override
    protected Result execCommand(CommandSender sender, Queue<String> args) {
        String name = args.remove();
        final BigDecimal balance;
        try {
            balance = args.isEmpty() ? repository.defaultBalance : new BigDecimal(args.element());
        } catch (NumberFormatException e) {
            sender.sendMessage(message.invalidArgument.toString("value", args.element()));
            return Result.ERROR;
        }

        registry.getUUIDAsync(name).thenAcceptSync(uuid -> {
            TemplateVariable variable = StringVariable.init().put("name", name);
            if (!uuid.isPresent()) {
                sender.sendMessage(message.playerNotFound.toString(variable));
                return;
            }
            if (repository.hasAccount(uuid.get())) {
                sender.sendMessage(message.createAlready.toString(variable));
                return;
            }

            repository.createAccount(uuid.get(), balance);
            sender.sendMessage(message.create.toString(variable.put("balance", repository.format(balance))));
        });
        return Result.OK;
    }

    @Override
    protected List<String> execTabComplete(CommandSender sender, Deque<String> args) {
        return CommandUtils.tabCompletePlayer(args);
    }

    @Override
    protected int minimumArgs() {
        return 1;
    }

    @Override
    protected String requirePermission() {
        return "jecon.create";
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money create <player> [balance]",
            message.help.create.toString(),
            "/money create notch",
            "/money create notch 100"
        );
    }
}

package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.repository.BalanceRepository;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public class Give extends SubCommand {
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Give(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Result execCommand(CommandSender sender, Queue<String> args) {
        String to = args.remove();
        BigDecimal amount = CommandUtils.parseDecimal(args.element());
        if (amount == null) {
            sender.sendMessage(message.invalidArgument.toString("value", args.element()));
            return Result.OK;
        }

        registry.getUUIDAsync(to).thenAcceptSync(uuid -> {
            TemplateVariable variable = StringVariable.init().put("name", to);
            if (!uuid.isPresent()) {
                sender.sendMessage(message.playerNotFound.toString(variable));
                return;
            }
            if (!repository.hasAccount(uuid.get())) {
                sender.sendMessage(message.accountNotFound.toString(variable));
                return;
            }
            repository.deposit(uuid.get(), amount);
            sender.sendMessage(message.give.toString(variable.put("amount", repository.format(amount))));
        });
        return Result.OK;
    }

    @Override
    protected List<String> execTabComplete(CommandSender sender, Deque<String> args) {
        return CommandUtils.tabCompletePlayer(args);
    }

    @Override
    protected String requirePermission() {
        return "jecon.give";
    }

    @Override
    protected int minimumArgs() {
        return 2;
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money give <player> <amount>",
            message.help.give.toString(),
            "/money give notch 100"
        );
    }
}

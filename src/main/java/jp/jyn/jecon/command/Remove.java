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
import java.util.Optional;
import java.util.Queue;

public class Remove extends SubCommand {
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Remove(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @Override
    protected Result execCommand(CommandSender sender, Queue<String> args) {
        registry.getUUIDAsync(args.element()).thenAcceptSync(uuid -> {
            TemplateVariable variable = StringVariable.init().put("name", args.element());
            if (!uuid.isPresent()) {
                sender.sendMessage(message.playerNotFound.toString(variable));
                return;
            }
            Optional<BigDecimal> balance = repository.getDecimal(uuid.get());
            if (!balance.isPresent()) {
                sender.sendMessage(message.accountNotFound.toString(variable));
                return;
            }

            repository.removeAccount(uuid.get());
            sender.sendMessage(message.remove.toString(variable.put("balance", repository.format(balance.get()))));

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
        return "jecon.remove";
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money remove <player>",
            message.help.remove.toString(),
            "/money remove notch"
        );
    }
}

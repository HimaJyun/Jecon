package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.BalanceRepository;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class Pay extends SubCommand {
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Pay(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Result execCommand(Player sender, Queue<String> args) {
        UUID from = sender.getUniqueId();
        String to = args.remove();
        BigDecimal amount = CommandUtils.parseDecimal(args.element());
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            sender.sendMessage(message.invalidArgument.toString("value", args.element()));
            return Result.OK;
        }

        // self check
        if (sender.getName().equalsIgnoreCase(to)) {
            sender.sendMessage(message.paySelf.toString());
            return Result.OK;
        }

        registry.getUUIDAsync(to).thenAcceptSync(uuid -> {
            if (!uuid.isPresent()) {
                sender.sendMessage(message.playerNotFound.toString("name", to));
                return;
            }
            if (!repository.has(from, amount)) {
                sender.sendMessage(message.notEnough.toString());
                return;
            }
            if (!repository.hasAccount(uuid.get())) {
                sender.sendMessage(message.accountNotFound.toString("name", to));
                return;
            }

            // pay money
            repository.withdraw(from, amount);
            repository.deposit(uuid.get(), amount);

            // send message
            TemplateVariable variable = StringVariable.init().put("amount", repository.format(amount));
            sender.sendMessage(message.paySuccess.toString(variable.put("name", to)));

            // If the recipient is online send a message
            Player player = Bukkit.getPlayer(uuid.get());
            if (player != null) {
                player.sendMessage(message.payReceive.toString(variable.put("name", sender.getName())));
            }
        });
        return Result.OK;
    }

    @Override
    protected List<String> execTabComplete(CommandSender sender, Deque<String> args) {
        return CommandUtils.tabCompletePlayer(args);
    }

    @Override
    protected boolean isPlayerOnly() {
        return true;
    }

    @Override
    protected String requirePermission() {
        return "jecon.pay";
    }

    @Override
    protected int minimumArgs() {
        return 2;
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money pay <player> <amount>",
            message.help.pay.toString(),
            "/money pay notch 100"
        );
    }
}

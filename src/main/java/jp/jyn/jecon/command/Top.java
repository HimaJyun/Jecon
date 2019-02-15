package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.command.SubCommand;
import jp.jyn.jbukkitlib.config.parser.template.variable.StringVariable;
import jp.jyn.jbukkitlib.config.parser.template.variable.TemplateVariable;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.BalanceRepository;
import jp.jyn.jecon.config.MessageConfig;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class Top extends SubCommand {
    private final static int ENTRY_PER_PAGE = 10;
    private final MessageConfig message;
    private final UUIDRegistry registry;
    private final BalanceRepository repository;

    public Top(MessageConfig message, UUIDRegistry registry, BalanceRepository repository) {
        this.message = message;
        this.registry = registry;
        this.repository = repository;
    }

    @Override
    protected Result execCommand(CommandSender sender, Queue<String> args) {
        final int page;
        try {
            page = args.isEmpty() ? 1 : Integer.parseInt(args.element());
        } catch (NumberFormatException e) {
            sender.sendMessage(message.invalidArgument.toString("value", args.element()));
            return Result.ERROR;
        }

        int offset = (page - 1) * ENTRY_PER_PAGE;
        Map<UUID, BigDecimal> top = repository.top(ENTRY_PER_PAGE, offset);
        registry.getMultipleNameAsync(top.keySet()).thenAcceptSync(uuidMap -> {
            TemplateVariable variable = StringVariable.init().put("page", page);
            sender.sendMessage(message.topFirst.toString(variable));

            int i = offset;
            for (Map.Entry<UUID, BigDecimal> entry : top.entrySet()) {
                variable.put("name", uuidMap.getOrDefault(entry.getKey(), "Unknown"));
                variable.put("uuid", entry.getKey()); // Secret variable
                variable.put("balance", repository.format(entry.getValue()));
                variable.put("rank", ++i);
                sender.sendMessage(message.topEntry.toString(variable));
            }
        });
        return Result.OK;
    }

    @Override
    protected List<String> execTabComplete(CommandSender sender, Deque<String> args) {
        return Collections.emptyList();
    }

    @Override
    protected String requirePermission() {
        return "jecon.top";
    }

    @Override
    public CommandHelp getHelp() {
        return new CommandHelp(
            "/money top [page]",
            message.help.top.toString(),
            "/money top",
            "/money top 1"
        );
    }
}

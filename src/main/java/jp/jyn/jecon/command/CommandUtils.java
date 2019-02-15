package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.util.PackagePrivate;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

// it is bad practice.
@PackagePrivate
class CommandUtils {
    private CommandUtils() {}

    @PackagePrivate
    static BigDecimal parseDecimal(String str) {
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    @PackagePrivate
    static List<String> tabCompletePlayer(Deque<String> args) {
        if (args.size() != 1) {
            return Collections.emptyList();
        }

        return Bukkit.getOnlinePlayers()
            .stream()
            .map(HumanEntity::getName)
            .filter(str -> str.startsWith(args.removeFirst()))
            .collect(Collectors.toList());
    }
}

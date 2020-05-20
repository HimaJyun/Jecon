package jp.jyn.jecon.command;

import jp.jyn.jbukkitlib.util.PackagePrivate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

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

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        List<String> complete = new ArrayList<>(players.size());
        String arg = args.getFirst();
        for (Player player : players) {
            String name = player.getName();
            if (name.startsWith(arg)) {
                complete.add(name);
            }
        }
        return complete;
    }
}

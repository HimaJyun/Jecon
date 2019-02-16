package jp.jyn.jecon;

import jp.jyn.jbukkitlib.util.PackagePrivate;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.repository.BalanceRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.math.BigDecimal;
import java.util.UUID;

@SuppressWarnings("unused")
@PackagePrivate
class EventListener implements Listener {
    private final boolean createAccountOnJoin;
    private final BigDecimal defaultBalance;

    private final VersionChecker checker;
    private final BalanceRepository repository;

    @PackagePrivate
    EventListener(MainConfig config, VersionChecker checker, BalanceRepository repository) {
        this.createAccountOnJoin = config.createAccountOnJoin;
        this.defaultBalance = config.defaultBalance;

        this.checker = checker;
        this.repository = repository;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("jecon.version")) {
            checker.check(player);
        }

        if (createAccountOnJoin) {
            repository.createAccount(player.getUniqueId(), defaultBalance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        repository.save(uuid);
    }
}

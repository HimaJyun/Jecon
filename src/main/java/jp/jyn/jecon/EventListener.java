package jp.jyn.jecon;

import jp.jyn.jbukkitlib.util.PackagePrivate;
import jp.jyn.jecon.config.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@PackagePrivate
class EventListener implements Listener {
    private final MainConfig config;
    private final VersionChecker checker;
    private final BalanceRepository repository;

    @PackagePrivate
    EventListener(MainConfig config, VersionChecker checker, BalanceRepository repository) {
        this.config = config;
        this.checker = checker;
        this.repository = repository;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("jecon.version")) {
            checker.check(player);
        }

        if (config.createAccountOnJoin) {
            repository.createAccount(player.getUniqueId(), repository.defaultBalance);
        }
    }
}

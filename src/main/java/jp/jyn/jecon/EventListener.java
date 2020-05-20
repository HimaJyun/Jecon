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
import java.util.function.Consumer;

@SuppressWarnings("unused")
@PackagePrivate
class EventListener implements Listener {
    private final boolean createAccountOnJoin;
    private final BigDecimal defaultBalance;

    private final VersionChecker checker;
    private final BalanceRepository repository;

    private final Consumer<UUID> consistency;
    private final Consumer<UUID> save;

    @PackagePrivate
    EventListener(MainConfig config, VersionChecker checker, BalanceRepository repository,
                  Consumer<UUID> consistency, Consumer<UUID> save) {
        this.createAccountOnJoin = config.createAccountOnJoin;
        this.defaultBalance = config.defaultBalance;

        this.checker = checker;
        this.repository = repository;
        this.consistency = consistency;
        this.save = save;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("jecon.version")) {
            checker.check(player);
        }

        // ログイン時に一貫性チェック
        consistency.accept(player.getUniqueId());

        if (createAccountOnJoin) {
            repository.createAccount(player.getUniqueId(), defaultBalance);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        save.accept(e.getPlayer().getUniqueId());
    }
}

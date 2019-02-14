package jp.jyn.jecon;

import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.config.ConfigLoader;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.config.MessageConfig;
import jp.jyn.jecon.db.Database;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;

public class Jecon extends JavaPlugin {
    private static Jecon instance = null;

    private ConfigLoader config;
    private BalanceRepository repository;

    // Stack(LIFO)
    private final Deque<Runnable> destructor = new ArrayDeque<>();

    @Override
    public void onEnable() {
        instance = this;
        destructor.clear();

        if (config == null) {
            config = new ConfigLoader();
        }
        config.reloadConfig();
        MainConfig main = config.getMainConfig();
        MessageConfig message = config.getMessageConfig();

        UUIDRegistry registry = UUIDRegistry.getSharedCacheRegistry(this);

        Database db = Database.connect(main.database);
        destructor.addFirst(db::close);

        repository = new BalanceRepository(main, db);
        destructor.addFirst(() -> repository = null);

        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (vault.isEnabled()) {
                vaultHook(registry);
            } else {
                getServer().getPluginManager().registerEvents(new VaultRegister(registry), this);
            }
        }
    }

    private void vaultHook(UUIDRegistry registry) {
        getServer().getServicesManager().register(
            Economy.class,
            new VaultEconomy(registry, this.config.getMainConfig(), this.repository),
            this,
            ServicePriority.Normal
        );
        this.destructor.addFirst(() -> getServer().getServicesManager().unregisterAll(this));
        getLogger().info("Hooked Vault");
    }

    @Override
    public void onDisable() {
        while (!destructor.isEmpty()) {
            destructor.removeFirst().run();
        }
    }

    public static Jecon getInstance() {
        return instance;
    }

    public BalanceRepository getRepository() {
        return repository;
    }

    private static class VaultRegister implements Listener {
        private final UUIDRegistry registry;

        public VaultRegister(UUIDRegistry registry) {
            this.registry = registry;
        }

        @EventHandler(ignoreCancelled = true)
        public void onPluginEnable(PluginEnableEvent e) {
            if (!e.getPlugin().getName().equals("Vault")) {
                return;
            }
            Jecon jecon = Jecon.getInstance();
            jecon.vaultHook(registry);
            PluginEnableEvent.getHandlerList().unregister(jecon);
        }
    }
}

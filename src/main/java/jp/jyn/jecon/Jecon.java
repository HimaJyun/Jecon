package jp.jyn.jecon;

import jp.jyn.jbukkitlib.command.SubExecutor;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.command.Convert;
import jp.jyn.jecon.command.Create;
import jp.jyn.jecon.command.Give;
import jp.jyn.jecon.command.Help;
import jp.jyn.jecon.command.Pay;
import jp.jyn.jecon.command.Reload;
import jp.jyn.jecon.command.Remove;
import jp.jyn.jecon.command.Set;
import jp.jyn.jecon.command.Show;
import jp.jyn.jecon.command.Take;
import jp.jyn.jecon.command.Top;
import jp.jyn.jecon.command.Version;
import jp.jyn.jecon.config.ConfigLoader;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.config.MessageConfig;
import jp.jyn.jecon.db.Database;
import jp.jyn.jecon.repository.BalanceRepository;
import jp.jyn.jecon.repository.LazyRepository;
import jp.jyn.jecon.repository.SyncRepository;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.Deque;

public class Jecon extends JavaPlugin {
    private static Jecon instance = null;

    private ConfigLoader config;
    private BalanceRepository repository;
    private VaultEconomy economy;

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

        VersionChecker checker = new VersionChecker(main.versionCheck, message);
        BukkitTask task = getServer().getScheduler().runTaskLater(
            this,
            () -> checker.check(Bukkit.getConsoleSender()), 20 * 30
        );
        destructor.addFirst(task::cancel);

        // connect db
        Database db = Database.connect(main.database);
        destructor.addFirst(db::close);

        // init repository
        repository = main.lazyWrite ? new LazyRepository(main, db) : new SyncRepository(main, db);
        destructor.addFirst(() -> {
            repository.saveAll();
            repository = null;
        });

        // register vault
        if (economy == null) {
            Plugin vault = getServer().getPluginManager().getPlugin("Vault");
            if (vault != null) {
                if (vault.isEnabled()) {
                    vaultHook(registry);
                } else {
                    getServer().getPluginManager().registerEvents(new VaultRegister(registry), this);
                }
            }
        } else {
            economy.init(main, registry, repository);
        }

        // register events
        getServer().getPluginManager().registerEvents(new EventListener(main, checker, repository), this);
        destructor.addFirst(() -> HandlerList.unregisterAll(this));

        // register commands
        SubExecutor.Builder builder = SubExecutor.Builder.init()
            .setDefaultCommand("show")
            .putCommand("show", new Show(message, registry, repository))
            .putCommand("pay", new Pay(message, registry, repository))
            .putCommand("set", new Set(message, registry, repository))
            .putCommand("give", new Give(message, registry, repository))
            .putCommand("take", new Take(message, registry, repository))
            .putCommand("create", new Create(main, message, registry, repository))
            .putCommand("remove", new Remove(message, registry, repository))
            .putCommand("top", new Top(message, registry, repository))
            .putCommand("convert", new Convert(config, repository, db))
            .putCommand("reload", new Reload(message))
            .putCommand("version", new Version(message, checker));
        Help help = new Help(message, builder.getSubCommands());
        builder.setErrorExecutor(help).putCommand("help", help);

        PluginCommand cmd = getCommand("jecon");
        SubExecutor subExecutor = builder.register(cmd);
        destructor.addFirst(() -> {
            cmd.setTabCompleter(this);
            cmd.setExecutor(this);
        });
    }

    private void vaultHook(UUIDRegistry registry) {
        if (economy != null) {
            return;
        }

        economy = new VaultEconomy(config.getMainConfig(), registry, repository);
        getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Normal);
        getLogger().info("Hooked Vault");
    }

    @Override
    public void onDisable() {
        while (!destructor.isEmpty()) {
            destructor.removeFirst().run();
        }
    }

    /**
     * Get Jecon instance
     *
     * @return Jecon
     */
    public static Jecon getInstance() {
        return instance;
    }

    /**
     * Get BalanceRepository
     *
     * @return BalanceRepository
     */
    public BalanceRepository getRepository() {
        return repository;
    }

    private static class VaultRegister implements Listener {
        private final UUIDRegistry registry;

        private VaultRegister(UUIDRegistry registry) {
            this.registry = registry;
        }

        @EventHandler(ignoreCancelled = true)
        public void onPluginEnable(PluginEnableEvent e) {
            if (!e.getPlugin().getName().equals("Vault")) {
                return;
            }
            Jecon.getInstance().vaultHook(registry);
            PluginEnableEvent.getHandlerList().unregister(this);
        }
    }
}

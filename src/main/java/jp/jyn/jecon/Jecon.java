package jp.jyn.jecon;

import jp.jyn.jbukkitlib.command.SubExecutor;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
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
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
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

        // connect db
        Database db = Database.connect(main.database);
        destructor.addFirst(db::close);

        // init repository
        repository = new BalanceRepository(main, db);
        destructor.addFirst(() -> repository = null);

        // register vault
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (vault.isEnabled()) {
                vaultHook(registry);
            } else {
                getServer().getPluginManager().registerEvents(new VaultRegister(registry), this);
            }
        }

        // register commands
        SubExecutor.Builder builder = SubExecutor.Builder.init()
            .setDefaultCommand("show")
            .putCommand("show", new Show(message, registry, repository))
            .putCommand("pay", new Pay(message, registry, repository))
            .putCommand("set", new Set(message, registry, repository))
            .putCommand("give", new Give(message, registry, repository))
            .putCommand("take", new Take(message, registry, repository))
            .putCommand("create", new Create(message, registry, repository))
            .putCommand("remove", new Remove(message, registry, repository))
            .putCommand("top", new Top(message, registry, repository))
            .putCommand("reload", new Reload(message))
            .putCommand("version", new Version(message));
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

        private VaultRegister(UUIDRegistry registry) {
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

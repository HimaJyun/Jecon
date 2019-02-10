package jp.jyn.jecon;

import jp.jyn.jecon.config.ConfigLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;

public class Jecon extends JavaPlugin {
    private static Jecon instance = null;

    private ConfigLoader config;

    // Stack(LIFO)
    private final Deque<Runnable> destructor = new ArrayDeque<>();

    /**
     * プラグインを有効化します、状態を判断してリロードを行います。
     */
    @Override
    public void onEnable() {
        instance = this;
        destructor.clear();

        if (config == null) {
            config = new ConfigLoader();
        }
        config.reloadConfig();

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
}

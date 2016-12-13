package jp.jyn.jecon;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import jp.jyn.jecon.command.Executer;
import jp.jyn.jecon.config.ConfigStruct;
import jp.jyn.jecon.config.MessageStruct;
import jp.jyn.jecon.db.Database;
import jp.jyn.jecon.db.rdbms.MySQL;
import jp.jyn.jecon.db.rdbms.SQLite;
import jp.jyn.jecon.listener.Login;
import net.milkbowl.vault.economy.Economy;

public class Jecon extends JavaPlugin {

	private ConfigStruct config = null;
	private MessageStruct message = null;
	private Database db = null;
	private boolean setupSuccess = false;

	/**
	 * プラグインを有効化します、状態を判断してリロードを行います。
	 */
	@Override
	public void onEnable() {
		// どれかがnullでなければリロード、もしくは起動失敗
		if (config != null || message != null || db != null) {
			// 無効化
			onDisable();
		}

		// nullなら読み込み、違えばリロード
		config = (config == null ? new ConfigStruct(this) : config.reloadConfig());
		message = (message == null ? new MessageStruct(this) : message.reloadConfig());

		// データベースをロード
		db = config.isMysql() ? new MySQL(this) : new SQLite(this);

		// ログインイベントを有効化
		new Login(this);

		// Vaultが有効かチェック
		new Listener() {
			private Jecon jecon;

			void setJecon(Jecon jecon) {
				this.jecon = jecon;
				Plugin pl = jecon.getServer().getPluginManager().getPlugin("Vault");
				if (pl == null) {
					// Vaultが無い
					return;
				} else if (!pl.isEnabled()) {
					// あるけど起動してない
					jecon.getServer().getPluginManager().registerEvents(this, jecon);
				} else {
					// あるし起動もしてる
					vaultHook();
				}
			}

			private void vaultHook() {
				// Jeconを登録
				jecon.getServer().getServicesManager().register(
						Economy.class,
						new VaultEconomy(jecon),
						jecon,
						ServicePriority.Normal);
			}

			@EventHandler
			public void plEnable(PluginEnableEvent e) {
				// Vaultが有効化された
				if (e.getPlugin().getName().equals("Vault")) {
					vaultHook();
					// 登録解除
					PluginEnableEvent.getHandlerList().unregister(jecon);
				}
			}
		}.setJecon(this);

		// コマンドを登録
		getCommand("money").setExecutor(new Executer(this));

		// セットアップ成功を示す
		setupSuccess = true;
	}

	@Override
	public void onDisable() {
		// 未セットアップに戻す
		setupSuccess = false;

		// コマンド登録解除
		getCommand("money").setExecutor(this);
		// Vaultフック解除
		getServer().getServicesManager().unregisterAll(this);
		// イベント登録解除
		HandlerList.unregisterAll(this);

		// データベースを閉じる
		if (db != null) {
			db.close();
		}
	}

	/**
	 * 通常ここが呼び出される事はありません。<br>
	 * 何らかのエラーでプラグインの起動に失敗した場合などに呼び出されます。(と、思います。)
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// 引数チェック
		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			// 権限チェック
			if (!sender.hasPermission("jecon.reload")) {
				// エラーメッセージ([Jecon] You don't have permission!!)
				sender.sendMessage("[" + ChatColor.RED + "Jecon" + ChatColor.RESET + "] You don't have permission!!");
				return true;
			}
			// リロード
			onEnable();
			// リロード完了のメッセージ([Jecon] Config has been reloaded!!)
			sender.sendMessage("[" + ChatColor.GREEN + "Jecon" + ChatColor.RESET
					+ "] Config has been reloaded!!");
		} else {
			// エラーメッセージ([Jecon] Initialization Error!!)
			sender.sendMessage("[" + ChatColor.RED + "Jecon" + ChatColor.RESET + "] Initialization "
					+ ChatColor.RED + "Error" + ChatColor.RESET + "!!");
			// 管理人に連絡して下さいメッセージ([Jecon] Plase contact the administrator!!)
			sender.sendMessage("[" + ChatColor.RED + "Jecon" + ChatColor.RESET + "] Please " + ChatColor.BOLD
					+ "contact" + ChatColor.RESET + " the " + ChatColor.BOLD + "administrator" + ChatColor.RESET
					+ "!!");
		}
		return true;
	}

	/**
	 * 設定を取得します。
	 * @return 読み込み済みのConfigStruct
	 */
	public ConfigStruct getConfigStruct() {
		return config;
	}

	/**
	 * メッセージ設定を読み込みます。
	 * @return 読み込み済みのMessageStruct
	 */
	public MessageStruct getMessageStruct() {
		return message;
	}

	/**
	 * セットアップが正常に完了しているか確認します。
	 * @return 正常に終了していればtrue、何らかのエラーで出来ていなければfalse
	 */
	public boolean isSetupSuccess() {
		return setupSuccess;
	}

	/**
	 * DataBaseクラスのインスタンスを取得します。
	 * @return インスタンス化済みのDataBase
	 */
	public Database getDb() {
		return db;
	}
}

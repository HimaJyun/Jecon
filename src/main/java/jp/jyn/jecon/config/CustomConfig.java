package jp.jyn.jecon.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class CustomConfig {

	/**　　　＿＿＿＿
	 * 　　r勺z勺z勺ｭ＼
	 * 　〈/⌒⌒⌒＼乙 ヽ
	 * 　//　 |　|　ヽ〉|
	 * ／｜ /ﾚ| Ｎ∧ |＼|
	 * ７ ﾚｲ=ｭヽ|r=ヽ|＿＞
	 * `ﾚ|ﾊ|Oｿ　 ﾋOｿＶ|Ｎ　＜ｵｼﾞｮｳｻﾏｰ
	 * 　(人ﾞ `＿　ﾞ(ｿ从
	 * 　(ｿﾚ＞――＜(ｿﾉ
	 * 　(ｿ｜ ﾚ|/L/ (ｿ
	 * ［>ヘL/ 只 L[>O<]
	 * （⌒O｜んz>/ /⌒}
	 * ⊂ニ⊃L　 / ∩＜
	 * / /＼/＼[(⌒)|二フ
	 * )ﾉ　/　　 ￣∪ﾀ＼
	 * 　 /ﾋ辷辷辷辷ﾀ　 >
	 * 　 ＼＿＿＿＿＿／
	 * 　　 |　/ |　/
	 */

	private FileConfiguration config = null;
	private final File configFile;
	private final String file;
	private final Plugin plugin;

	/**
	 * config.ymlを設定として読み書きするカスタムコンフィグクラスをインスタンス化します。
	 *
	 * @param plugin
	 *            ロード対象のプラグイン
	 */
	CustomConfig(Plugin plugin) {
		this(plugin, "config.yml");
	}

	/**
	 * 指定したファイル名で設定を読み書きするカスタムコンフィグクラスをインスタンス化します。
	 *
	 * @param plugin
	 *            ロード対象のプラグイン
	 * @param fileName
	 *            読み込みファイル名
	 */
	CustomConfig(Plugin plugin, String fileName) {
		this.plugin = plugin;
		this.file = fileName;
		configFile = new File(plugin.getDataFolder(), file);
	}

	/**
	 * デフォルト設定を保存します。
	 */
	public void saveDefaultConfig() {
		if (!configFile.exists()) {
			plugin.saveResource(file, false);
		}
	}

	/**
	 * 読み込んだFileConfiguretionを提供します。
	 *
	 * @return 読み込んだ設定
	 */
	public FileConfiguration getConfig() {
		if (config == null) {
			reloadConfig();
		}
		return config;
	}

	/**
	 * 設定を保存します。
	 */
	public void saveConfig() {
		if (config == null) {
			return;
		}
		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
		}
	}

	/**
	 * 設定をリロードします。
	 */
	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(configFile);

		final InputStream defConfigStream = plugin.getResource(file);
		if (defConfigStream == null) {
			return;
		}

		config.setDefaults(
				YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
	}

	/**
	 * 色コードを置換します、置換される文字列は以下の通りです。<br>
	 * &0->Black(#000000)<br>
	 * &1->Dark Blue(#0000AA)<br>
	 * &2->Dark Green(#00AA00)<br>
	 * &3->Dark Aqua(#00AAAA)<br>
	 * &4->Dark Red(#AA0000)<br>
	 * &5->Purple(#AA00AA)<br>
	 * &6->Gold(#FFAA00)<br>
	 * &7->Gray(#AAAAAA)<br>
	 * &8->Dark Gray(#555555)<br>
	 * &9->Blue(#5555FF)<br>
	 * &a->Green(#55FF55)<br>
	 * &b->Aqua(#55FFFF)<br>
	 * &c->Red(#FF5555)<br>
	 * &d->Light Purple(#FF55FF)<br>
	 * &e->Yellow(#FFFF55)<br>
	 * &f->White(#FFFFFF)<br>
	 * &k->Obfuscated<br>
	 * &l->Bold<br>
	 * &m->Strikethrough<br>
	 * &n->Underline<br>
	 * &o->Italic<br>
	 * &r->Reset<br>
	 * &&で上記の変換を無効化出来ます。
	 * @param str 置換対象の色コード
	 * @return 置換後の色コード、入力がnullならnull
	 */
	public String replaceColor(String str) {
		return str == null ? null : ChatColor.translateAlternateColorCodes('&', str);
	}
}

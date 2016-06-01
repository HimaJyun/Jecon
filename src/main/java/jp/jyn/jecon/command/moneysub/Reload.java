package jp.jyn.jecon.command.moneysub;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.MessageStruct;

public class Reload implements MoneyCommand {

	private final Jecon jecon;
	private final MessageStruct message;

	public Reload(Jecon jecon) {
		this.jecon = jecon;
		message = jecon.getMessageStruct();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		// 権限チェック
		if (!sender.hasPermission("jecon.reload")) {
			sender.sendMessage(message.getDontHavePermission());
			return;
		}
		// リロード
		jecon.onEnable();
		// リロード完了のメッセージ
		sender.sendMessage("[" + ChatColor.GREEN + "Jecon" + ChatColor.RESET + "] Config has been reloaded!!");
	}
}

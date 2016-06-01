package jp.jyn.jecon.command.moneysub;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.MessageStruct;
import jp.jyn.jecon.db.Database;
import jp.jyn.jecon.db.Database.Reason;

public class Pay implements MoneyCommand {

	private final MessageStruct message;
	private final Database db;
	private final Jecon jecon;

	public Pay(Jecon jecon) {
		this.jecon = jecon;

		message = jecon.getMessageStruct();
		db = jecon.getDb();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		// 権限チェック
		if (!sender.hasPermission("jecon.pay")) {
			sender.sendMessage(message.getDontHavePermission());
			return;
		}

		// プレイヤーチェック
		if (!(sender instanceof Player)) {
			sender.sendMessage(MessageStruct.PLAYER_ONRY);
			return;
		}

		// 引数チェック
		if (args.length < 3) {
			sender.sendMessage(message.getHelpPay());
			return;
		}

		double amount;
		// 金額指定をパース
		try {
			amount = Double.parseDouble(args[2]);
			// 0以下なら不正
			if (amount < 0) {
				sender.sendMessage(message.getInvalidAmount());
				return;
			}
		} catch (NumberFormatException e) {
			sender.sendMessage(message.getInvalidAmount());
			return;
		}

		// アカウント存在チェック
		if (!db.hasAccount(args[1])) {
			sender.sendMessage(message.getAccountNotFound());
			return;
		}

		String result;
		String displayAmount = db.format(amount);
		// まずは出金
		Reason reason = db.withdrawPlayer((Player) sender, amount);

		switch (reason) {
		case ACCOUNT_NOT_FOUND:
			result = message.getAccountNotFound();
			break;
		case NOT_ENOUGH:
			result = message.getPayNotEnough();
			break;
		case SUCCESS:
			result = message.getPaySuccess();
			break;
		default:
			result = message.getUnknownError();
			break;
		}
		sender.sendMessage(result
				.replace(MessageStruct.MACRO_PLAYER, args[1])
				.replace(MessageStruct.MACRO_BALANCE, displayAmount));
		// 成功以外ならエラーで帰る
		if (reason != Reason.SUCCESS) {
			return;
		}

		// 入金
		switch (db.depositPlayer(args[1], amount)) {
		case SUCCESS:
			Player pl = jecon.getServer().getPlayer(args[1]);
			if (pl != null) {
				pl.sendMessage(message.getPayReceive()
						.replace(MessageStruct.MACRO_PLAYER, sender.getName())
						.replace(MessageStruct.MACRO_BALANCE, displayAmount));
			}
			return;
		default:
			sender.sendMessage(message.getUnknownError());
			return;
		}
	}

}

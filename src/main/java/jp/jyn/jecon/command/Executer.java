package jp.jyn.jecon.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.moneysub.Create;
import jp.jyn.jecon.command.moneysub.Give;
import jp.jyn.jecon.command.moneysub.Help;
import jp.jyn.jecon.command.moneysub.NoArgs;
import jp.jyn.jecon.command.moneysub.Pay;
import jp.jyn.jecon.command.moneysub.Reload;
import jp.jyn.jecon.command.moneysub.Remove;
import jp.jyn.jecon.command.moneysub.Set;
import jp.jyn.jecon.command.moneysub.Take;
import jp.jyn.jecon.command.moneysub.Top;

public class Executer implements CommandExecutor {

	private final Map<String, MoneyCommand> subCommands;
	// 空白を含む事で引数としてあり得ない形にする
	private final String NO_ARGS = "NO ARGS";

	public Executer(Jecon jecon) {
		Map<String, MoneyCommand> commands = new HashMap<>();
		commands.put(NO_ARGS, new NoArgs(jecon));

		commands.put("pay", new Pay(jecon));
		commands.put("top", new Top(jecon));
		commands.put("give", new Give(jecon));
		commands.put("take", new Take(jecon));
		commands.put("set", new Set(jecon));
		commands.put("create", new Create(jecon));
		commands.put("remove", new Remove(jecon));
		commands.put("reload", new Reload(jecon));
		commands.put("help", new Help(jecon));
		subCommands = Collections.unmodifiableMap(commands);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		subCommands.get(
				(args.length > 0 && subCommands.containsKey(args[0].toLowerCase(Locale.ENGLISH)))
						? args[0].toLowerCase(Locale.ENGLISH)
						: NO_ARGS)
				.onCommand(sender, args);
		/*
		// 引数が1以上で、サブコマンドとして登録されている場合
		if (args.length > 0 && subCommands.containsKey(args[0].toLowerCase())) {
			// サブクラスに投げる
			return subCommands.get(args[0].toLowerCase()).onCommand(sender, args);
			// それ以外(引数無し=残高確認,その他引数=他ユーザの所持金確認)
		} else {
			// 引数無しに投げる
			return subCommands.get(NO_ARGS).onCommand(sender, args);
		}
		*/
		// 常時true(falseにしても要らないメッセージが出るだけ、こちら側で出すので)
		return true;
	}

}

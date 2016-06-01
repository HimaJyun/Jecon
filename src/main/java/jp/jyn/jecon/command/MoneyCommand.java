package jp.jyn.jecon.command;

import org.bukkit.command.CommandSender;

public interface MoneyCommand {
	abstract void onCommand(CommandSender sender, String[] args);
}

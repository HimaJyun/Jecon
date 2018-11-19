package jp.jyn.jecon.command.moneysub;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.MessageStruct;

public class Help implements MoneyCommand {

    private final MessageStruct message;

    public Help(Jecon jecon) {
        message = jecon.getMessageStruct();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.WHITE + " Jecon " + ChatColor.GREEN + "=========");

        sender.sendMessage(message.getHelpShow());
        sender.sendMessage(message.getHelpPay());

        sender.sendMessage(message.getHelpTop());

        sender.sendMessage(message.getHelpGive());
        sender.sendMessage(message.getHelpTake());
        sender.sendMessage(message.getHelpSet());

        sender.sendMessage(message.getHelpCreate());
        sender.sendMessage(message.getHelpRemove());

        sender.sendMessage(message.getHelpReload());
        sender.sendMessage(message.getHelpHelp());
    }
}

package jp.jyn.jecon.command.moneysub;

import org.bukkit.command.CommandSender;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.ConfigStruct;
import jp.jyn.jecon.config.MessageStruct;
import jp.jyn.jecon.db.Database;

public class Create implements MoneyCommand {

    private final ConfigStruct config;
    private final MessageStruct message;
    private final Database db;

    public Create(Jecon jecon) {
        config = jecon.getConfigStruct();
        message = jecon.getMessageStruct();
        db = jecon.getDb();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("jecon.create")) {
            sender.sendMessage(message.getDontHavePermission());
            return;
        }
        // 引数チェック
        if (args.length < 2) {
            sender.sendMessage(message.getHelpCreate());
            return;
        }

        double balance = config.getDefaultBalance();
        // 金額指定があるか
        if (args.length > 2) {
            // あればパース
            try {
                balance = Double.parseDouble(args[2]);
                // 0以下なら不正
                if (balance < 0) {
                    sender.sendMessage(message.getInvalidAmount());
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(message.getInvalidAmount());
                return;
            }
        }

        String result;
        if (db.hasAccount(args[1])) {
            result = message.getCreateExists();
        } else if (db.createPlayerAccount(args[1], balance)) {
            result = message.getCreateSuccess();
        } else {
            result = message.getUnknownError();
        }
        sender.sendMessage(result
            .replace(MessageStruct.MACRO_PLAYER, args[1])
            .replace(MessageStruct.MACRO_BALANCE, db.format(balance)));
    }

}

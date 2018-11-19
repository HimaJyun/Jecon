package jp.jyn.jecon.command.moneysub;

import org.bukkit.command.CommandSender;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.MessageStruct;
import jp.jyn.jecon.db.Database;

public class Set implements MoneyCommand {

    private final MessageStruct message;
    private final Database db;

    public Set(Jecon jecon) {
        message = jecon.getMessageStruct();
        db = jecon.getDb();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("jecon.set")) {
            sender.sendMessage(message.getDontHavePermission());
            return;
        }
        // 引数チェック
        if (args.length < 3) {
            sender.sendMessage(message.getHelpSet());
            return;
        }

        double balance;
        // 金額指定をパース
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

        String result;
        switch (db.set(args[1], balance)) {
            case ACCOUNT_NOT_FOUND:
                result = message.getAccountNotFound();
                break;
            case SUCCESS:
                result = message.getSetSuccess();
                break;
            default:
                result = message.getUnknownError();
                break;
        }

        sender.sendMessage(result
            .replace(MessageStruct.MACRO_PLAYER, args[1])
            .replace(MessageStruct.MACRO_BALANCE, db.format(balance)));
    }

}

package jp.jyn.jecon.command.moneysub;

import org.bukkit.command.CommandSender;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.MessageStruct;
import jp.jyn.jecon.db.Database;

public class Remove implements MoneyCommand {

    private final MessageStruct message;
    private final Database db;

    public Remove(Jecon jecon) {
        message = jecon.getMessageStruct();
        db = jecon.getDb();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("jecon.remove")) {
            sender.sendMessage(message.getDontHavePermission());
            return;
        }
        // 引数チェック
        if (args.length > 2) {
            sender.sendMessage(message.getHelpRemove());
            return;
        }

        // 残高取得
        double balance = db.getBalance(args[1]);

        String result;
        switch (db.removePlayerAccount(args[1])) {
            case ACCOUNT_NOT_FOUND:
                result = message.getAccountNotFound();
                break;
            case SUCCESS:
                result = message.getRemoveSuccess();
                break;
            default:
                result = message.getUnknownError();
        }
        // 削除したアカウントを通知
        sender.sendMessage(result
            .replace(MessageStruct.MACRO_PLAYER, args[1])
            .replace(MessageStruct.MACRO_BALANCE, db.format(balance)));
    }

}

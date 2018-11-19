package jp.jyn.jecon.command.moneysub;

import jp.jyn.jecon.Jecon;
import jp.jyn.jecon.command.MoneyCommand;
import jp.jyn.jecon.config.MessageStruct;
import jp.jyn.jecon.db.Database;
import jp.jyn.jecon.db.Database.Entry;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Top implements MoneyCommand {

    private final Database db;
    private final MessageStruct message;

    public Top(Jecon jecon) {
        db = jecon.getDb();
        message = jecon.getMessageStruct();
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("jecon.top")) {
            sender.sendMessage(message.getDontHavePermission());
            return;
        }
        int page = 1;

        // 引数チェック
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                // 0以下なら1に
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(message.getInvalidAmount());
                return;
            }
        }

        List<Entry> list = db.topList(page);
        // 空チェック
        if (list.isEmpty()) {
            sender.sendMessage(message.getTopEmpty());
            return;
        }

        sender.sendMessage(message.getTopFirst().replace(MessageStruct.MACRO_PAGE, String.valueOf(page)));
        int i = (page - 1) * 10;
        for (Entry entry : list) {
            ++i;
            sender.sendMessage(message.getTopEntry()
                .replace(MessageStruct.MACRO_RANK, String.valueOf(i))
                .replace(MessageStruct.MACRO_PLAYER, entry.user)
                .replace(MessageStruct.MACRO_BALANCE, db.format(entry.balance)));
        }
    }

}

package jp.jyn.jecon;

import jp.jyn.jecon.config.ConfigStruct;
import jp.jyn.jecon.db.Database;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

public class VaultEconomy implements Economy {

    // メインクラスのインスタンス
    private final Jecon jecon;
    // データベースのインスタンス
    private final Database db;
    // 設定
    private final ConfigStruct config;

    public VaultEconomy(Jecon jecon) {
        this.jecon = jecon;

        config = jecon.getConfigStruct();
        db = jecon.getDb();

        jecon.getLogger().info("Vault economy hooked.");
    }

    // ======== isEnabled ========

    @Override
    public boolean isEnabled() {
        return jecon.isSetupSuccess();
    }

    // ======== getName ========

    @Override
    public String getName() {
        return "Jecon";
    }

    // ======== format ========

    @Override
    public String format(double amount) {
        return db.format(amount);
    }

    // ======== fractionalDigits ========

    @Override
    public int fractionalDigits() {
        return -1;
    }

    // ======== currencyNamePlural ========

    @Override
    public String currencyNamePlural() {
        return config.getFormatMajorPlural();
    }

    // ======== currencyNameSingular ========

    @Override
    public String currencyNameSingular() {
        return config.getFormatMajorSingle();
    }

    // ======== createPlayerAccount ========

    @Override
    public boolean createPlayerAccount(String playerName) {
        return db.createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return db.createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    // ======== depositPlayer ========

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can't deposit negative amount");
        }
        switch (db.depositPlayer(playerName, amount)) {
            case ACCOUNT_NOT_FOUND:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account not found");
            case SUCCESS:
                return new EconomyResponse(amount, db.getBalance(playerName), ResponseType.SUCCESS, "");
            default:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Unknown error");
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can't deposit negative amount");
        }
        switch (db.depositPlayer(player, amount)) {
            case ACCOUNT_NOT_FOUND:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account not found");
            case SUCCESS:
                return new EconomyResponse(amount, db.getBalance(player), ResponseType.SUCCESS, "");
            default:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Unknown error");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    // ======== getBalance ========

    @Override
    public double getBalance(String playerName) {
        return db.getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return db.getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    // ======== has ========

    @Override
    public boolean has(String playerName, double amount) {
        return db.has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return db.has(player, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    // ======== hasAccount ========

    @Override
    public boolean hasAccount(String playerName) {
        return db.hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return db.hasAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    // ======== withdrawPlayer ========

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can't deposit negative amount");
        }
        switch (db.withdrawPlayer(playerName, amount)) {
            case ACCOUNT_NOT_FOUND:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account not found");
            case NOT_ENOUGH:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Money is not enough");
            case SUCCESS:
                return new EconomyResponse(amount, db.getBalance(playerName), ResponseType.SUCCESS, "");
            default:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Unknown error");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, ResponseType.FAILURE, "Can't deposit negative amount");
        }
        switch (db.withdrawPlayer(player, amount)) {
            case ACCOUNT_NOT_FOUND:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Account not found");
            case NOT_ENOUGH:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Money is not enough");
            case SUCCESS:
                return new EconomyResponse(amount, db.getBalance(player), ResponseType.SUCCESS, "");
            default:
                return new EconomyResponse(0, 0, ResponseType.FAILURE, "Unknown error");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    // ==========================
    //  銀行機能はサポートしない
    // ==========================

    @Override
    public boolean hasBankSupport() {
        // 銀行機能はないため常時false
        return false;
    }

    @Override
    public List<String> getBanks() {
        // 銀行機能はないため常時空リスト
        return Collections.emptyList();
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer offlinePlayer) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer offlinePlayer) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer offlinePlayer) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return notImplementedBank();
    }

    private EconomyResponse notImplementedBank() {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Jecon does not support bank.");
    }
}

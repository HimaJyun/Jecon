package jp.jyn.jecon;

import jp.jyn.jbukkitlib.util.PackagePrivate;
import jp.jyn.jbukkitlib.uuid.UUIDRegistry;
import jp.jyn.jecon.config.MainConfig;
import jp.jyn.jecon.repository.AbstractRepository;
import jp.jyn.jecon.repository.BalanceRepository;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@PackagePrivate
class VaultEconomy implements Economy {
    private final BigDecimal defaultBalance;

    private final UUIDRegistry registry;
    private final MainConfig config;
    private final BalanceRepository repository;

    @PackagePrivate
    VaultEconomy(MainConfig config, UUIDRegistry registry, BalanceRepository repository) {
        this.registry = registry;
        this.config = config;
        this.repository = repository;

        this.defaultBalance = config.defaultBalance;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Jecon";
    }

    @Override
    public int fractionalDigits() {
        /* Hint: How does Jecon keep decimals?
            Jecon multiplies the value up to two decimal places by 100 times and treats it.
            (This is the same mechanism as Raspberry Pi CPU temperature, It can be obtained from /sys/class/thermal/thermal_zone0/temp)
            In general, the value below the decimal point of the currency is 1/100 (cent, JPY '銭', etc.)

            Of course, it is also possible to make it more accurate. (However, in that case, the maximum value decreases)
            If you want it please post Issue.
         */
        return AbstractRepository.FRACTIONAL_DIGITS;
    }

    @Override
    public String format(double v) {
        return repository.format(v);
    }

    @Override
    public String currencyNamePlural() {
        return config.format.pluralMajor;
    }

    @Override
    public String currencyNameSingular() {
        return config.format.singularMajor;
    }

    @Override
    public boolean hasAccount(String s) {
        return registry.getUUID(s).map(repository::hasAccount).orElse(false);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return repository.hasAccount(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return hasAccount(s);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return hasAccount(offlinePlayer);
    }

    @Override
    public double getBalance(String s) {
        return registry.getUUID(s)
            .map(repository::getDouble)
            .orElse(OptionalDouble.empty())
            .orElse(0);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return repository.getDouble(offlinePlayer.getUniqueId()).orElse(0);
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(s);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String s, double v) {
        return registry.getUUID(s).map(uuid -> repository.has(uuid, v)).orElse(false);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return repository.has(offlinePlayer.getUniqueId(), v);
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return has(s, v);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return has(offlinePlayer, v);
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return registry.getUUID(s).map(uuid -> repository.createAccount(uuid, defaultBalance)).orElse(false);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return repository.createAccount(offlinePlayer.getUniqueId(), defaultBalance);
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return createPlayerAccount(s);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return createPlayerAccount(offlinePlayer);
    }

    private EconomyResponse withdrawPlayer(UUID uuid, double value) {
        if (value < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        if (repository.withdraw(uuid, value)) {
            return new EconomyResponse(0, repository.getDouble(uuid).orElse(0), EconomyResponse.ResponseType.SUCCESS, "OK");
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        return registry.getUUID(s)
            .map(uuid -> withdrawPlayer(uuid, v))
            .orElseGet(() -> new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User does not exist"));
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        return withdrawPlayer(offlinePlayer.getUniqueId(), v);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        return withdrawPlayer(s, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    private EconomyResponse depositPlayer(UUID uuid, double value) {
        if (value < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        if (repository.deposit(uuid, value)) {
            return new EconomyResponse(0, repository.getDouble(uuid).orElse(0), EconomyResponse.ResponseType.SUCCESS, "OK");
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account does not exist");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        return registry.getUUID(s)
            .map(uuid -> depositPlayer(uuid, v))
            .orElseGet(() -> new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "User does not exist"));
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        return depositPlayer(offlinePlayer.getUniqueId(), v);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        return depositPlayer(s, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        return depositPlayer(offlinePlayer, v);
    }

    // region bank
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return notImplementedBank();
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return notImplementedBank();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    private static EconomyResponse notImplementedBank() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Jecon does not support bank.");
    }
    // endregion
}

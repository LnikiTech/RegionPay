package com.example.region;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public class RegionPlugin extends JavaPlugin {

    private double taxRate;  // インスタンスフィールド
    private static final String TAX_ACCOUNT = "tax";

    private Economy economy;
    private FileConfiguration config;
    private PlayerMoveListener playerMoveListener;
    private ReloadCommand reloadCommand;
    private PlayerRegionSession playerRegionSession;
    private RegionFeeHandler feeHandler;  // RegionFeeHandler のインスタンスをフィールドに定義
    private RegionFeeManager regionFeeManager;

    @Override
    public void onLoad() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            Bukkit.getLogger().warning("[RegionPay] WorldGuardが見つかりません。プラグインを無効化します。");
            return;
        }
        CustomFlagHandler.registerFlags();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        economy = getEconomy();
        if (economy == null) {
            getLogger().warning("[RegionPay] Vault経済プラグインが見つかりません。");
            return;
        }

        taxRate = loadTaxRate();

        WorldGuardPlugin worldGuardPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            regionFeeManager = new RegionFeeManager(worldGuardPlugin);
            feeHandler = new RegionFeeHandler(economy, taxRate, regionFeeManager, this);  // RegionFeeHandler を初期化
        } else {
            getLogger().warning("[RegionPay] WorldGuardプラグインが見つかりません。料金設定をロードできません。");
            return;
        }

        // 料金を読み込む
        feeHandler.loadRegionFees();
        playerRegionSession = new PlayerRegionSession(feeHandler);
        playerMoveListener = new PlayerMoveListener(economy, playerRegionSession, feeHandler);

        reloadCommand = new ReloadCommand(this, feeHandler);
        getCommand("regionpay").setExecutor(reloadCommand);

        Bukkit.getPluginManager().registerEvents(playerMoveListener, this);

        getLogger().info("[RegionPay] RegionPay plugin is enabled!");
    }

    private double loadTaxRate() {
        return config.getDouble("tax-rate", 0.1); // デフォルトの税率は 0.1
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    @Override
    public void onDisable() {
        // feeHandlerがnullでないことを確認してから保存処理を行う
        if (feeHandler != null) {
            feeHandler.saveRegionFees();
        } else {
            getLogger().warning("[RegionPay] feeHandlerがnullのため、料金情報を保存できませんでした。");
        }
        getLogger().info("RegionPay plugin is disabled!");
    }

    public double getTaxRate() {
        return taxRate;
    }

    public RegionFeeHandler getFeeHandler() {
        return feeHandler;
    }

    public void reloadPlugin() {
        // config.ymlのリロード
        reloadConfig();

        // 設定ファイルから税率を再読み込み
        double newTaxRate = getConfig().getDouble("tax-rate", 0.1);  // デフォルト値を0.1に指定

        // 新しい税率を feeHandler に反映
        if (feeHandler != null) {
            feeHandler.setTaxRate(newTaxRate);  // 新しい税率をfeeHandlerに反映

            // 税率をログに表示
            getLogger().info("設定ファイルから読み込んだ税率: " + newTaxRate);
            getLogger().info("feeHandlerに反映された税率: " + feeHandler.getTaxRate());
        } else {
            getLogger().warning("[RegionPay] feeHandlerが初期化されていません！");
        }
    }
}
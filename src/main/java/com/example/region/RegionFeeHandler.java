package com.example.region;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RegionFeeHandler {
    private final Economy economy;
    private double taxRate;
    private static final String TAX_ACCOUNT = "tax";  // 税金口座名の定数
    private final Map<String, Double> regionFees = new HashMap<>();  // 各リージョンの料金
    private final RegionFeeManager regionFeeManager;
    private final JavaPlugin plugin;  // plugin 変数
    private File feeFile;
    private YamlConfiguration feeConfig;

    // コンストラクタで economy, taxRate, RegionFeeManager と plugin を受け取る
    public RegionFeeHandler(Economy economy, double taxRate, RegionFeeManager regionFeeManager, JavaPlugin plugin) {
        this.economy = economy;
        this.taxRate = taxRate;
        this.regionFeeManager = regionFeeManager;  // RegionFeeManager を初期化
        this.plugin = plugin;  // plugin を初期化

        // ファイルと設定の初期化
        this.feeFile = new File(plugin.getDataFolder(), "regionFees.yml");

        if (!feeFile.exists()) {
            try {
                feeFile.createNewFile();  // ファイルがなければ作成
                // ファイルを初期化 (デフォルト値)
                feeConfig = new YamlConfiguration();
                feeConfig.set("defaultRegion", 100.0);  // デフォルトのリージョン料金
                feeConfig.save(feeFile);
            } catch (IOException e) {
                plugin.getLogger().warning("料金設定ファイルを作成できませんでした！");
            }
        }

        feeConfig = YamlConfiguration.loadConfiguration(feeFile);  // YamlConfigurationを読み込む
        loadRegionFees();  // 初期ロード
    }

    // 税率を変更するためのメソッドを追加
    public void setTaxRate(double newTaxRate) {
        this.taxRate = newTaxRate;  // 新しい税率を設定
        plugin.getConfig().set("tax-rate", newTaxRate);  // 設定ファイルに新しい税率を保存
        plugin.saveConfig();  // 設定ファイルを保存

        // デバッグ用のログ
        plugin.getLogger().info("[RegionPay] 税率が変更されました: " + newTaxRate);
    }

    // 税率を取得するためのメソッドを追加
    public double getTaxRate() {
        return taxRate;  // 現在の税率を返す
    }

    // 料金の保存
    public void saveRegionFees() {
        // 保存前にファイルを更新
        for (Map.Entry<String, Double> entry : regionFees.entrySet()) {
            feeConfig.set(entry.getKey(), entry.getValue()); // リージョンの名前と料金を設定
        }

        try {
            feeConfig.save(feeFile); // ファイルに保存（同期的に保存）
        } catch (IOException e) {
            plugin.getLogger().warning("料金設定を保存できませんでした！");
        }
    }

    public void reloadConfig() {
        // config.yml をリロード
        plugin.reloadConfig();  // プラグインの設定をリロード

        // config.yml から税率を取得
        double newTaxRate = plugin.getConfig().getDouble("tax-rate", 0.1);  // デフォルト値を直接指定

        // 税率を更新
        setTaxRate(newTaxRate);

        // 新しい税率をログに表示（デバッグ用）
        plugin.getLogger().info("新しい税率が設定されました: " + newTaxRate);
        plugin.getLogger().info("現在の設定ファイルの税率: " + plugin.getConfig().getDouble("tax-rate"));
        plugin.getLogger().info("新しい税率: " + getTaxRate());
    }

    // 料金の読み込み
    public void loadRegionFees() {
        if (feeConfig == null) {
            plugin.getLogger().warning("設定ファイルが正常に読み込まれませんでした。");
            return;
        }

        for (String key : feeConfig.getKeys(false)) {
            regionFees.put(key, feeConfig.getDouble(key)); // ファイルから料金を読み込む
        }
    }

    // 料金を取得
    public double getRegionFee(String regionName) {
        if (!regionFees.containsKey(regionName)) {
            plugin.getLogger().warning("リージョン " + regionName + " の料金が設定されていません。");
            return 0.0;  // デフォルト料金や他の適切なデフォルト値を返すことができる
        }
        return regionFees.get(regionName);
    }

    // 料金の設定
    public void setRegionFee(String regionName, double feeAmount) {
        regionFees.put(regionName, feeAmount);  // 料金を設定
        saveRegionFees();  // 設定後に即座に保存
    }

    public boolean handleRegionFees(Player player, Set<String> regions) {
        StringBuilder message = new StringBuilder();
        boolean allFeesPaid = true; // すべての料金が支払われたかどうかを追跡

        for (String regionId : regions) {
            // オーナーかどうかをチェック
            Set<String> regionOwners = getRegionOwnersForRegion(player, regionId);
            if (regionOwners.contains(player.getName())) {
                continue; // オーナーの場合、料金請求をスキップ
            }

            // Player オブジェクトを渡すように修正
            if (isEntryFeeRequired(player, regionId)) {
                double entryFee = getEntryFeeForRegion(regionId);
                // chargePlayerEntryFee に regionId を渡す
                if (chargePlayerEntryFee(player, entryFee, regionId)) {
                    message.append(String.format("リージョン %s に入るために %.2f が請求されました。\n", regionId, entryFee));
                } else {
                    message.append("このリージョンに入るための所持金が不足しています。\n");
                    allFeesPaid = false; // 支払いが失敗した場合
                }
            }
        }

        if (message.length() > 0) {
            player.sendMessage(message.toString());
        }

        return allFeesPaid; // すべての料金が支払われたかどうかを返す
    }

    // リージョンに対して料金が必要かどうかをチェック
    public boolean isEntryFeeRequired(Player player, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));  // プレイヤーがいるワールドに対応

        if (regionManager == null) {
            return false;  // リージョンマネージャが存在しない場合
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region == null) {
            return false;  // リージョンが存在しない場合
        }

        // フラグが "ALLOW" の場合のみ料金が必要
        Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), "regionpay-entry-fee");

        // Flag<?>をStateFlagにキャスト
        if (flag instanceof StateFlag) {
            StateFlag stateFlag = (StateFlag) flag;

            // フラグが設定されているかつ料金が設定されている
            if (region.getFlag(stateFlag) == StateFlag.State.ALLOW) {
                return regionFees.containsKey(regionId);  // フラグが設定されているかつ料金が設定されている
            }
        }

        return false;
    }

    // リージョンに設定された入場料を取得
    private double getEntryFeeForRegion(String regionId) {
        return regionFees.getOrDefault(regionId, 0.0);  // 設定されていない場合は0円
    }

    // プレイヤーから入場料を徴収
    private boolean chargePlayerEntryFee(Player player, double entryFee, String regionId) {
        // 指定されたリージョンのオーナーを取得
        Set<String> regionOwners = getRegionOwnersForRegion(player, regionId);  // 特定のリージョンのオーナーを取得

        // プレイヤーがオーナーの場合は料金を徴収しない
        if (regionOwners.contains(player.getName())) {
            return true; // オーナーには料金を徴収しない
        }

        // 料金の支払い処理
        if (economy.has(player, entryFee)) {
            economy.withdrawPlayer(player, entryFee);
            distributeFeePayment(entryFee, player, regionOwners);  // オーナーへ送金
            return true;
        } else {
            // 所持金が不足している場合
            Bukkit.getLogger().warning("[Tax] プレイヤー " + player.getName() + " の所持金が不足しています。");
            player.sendMessage("所持金が不足しているため、リージョンに入れませんでした。");

            // 所持金が足りない場合、リージョンに入れない処理を追加
            return false;
        }
    }

    // 支払い金額を税金口座とオーナーに分配
    private void distributeFeePayment(double entryFee, Player player, Set<String> regionOwners) {
        double taxAmount = calculateTax(entryFee);  // 税金計算
        double ownerAmount = entryFee - taxAmount;

        // 税金口座に送金
        EconomyResponse taxResponse = economy.depositPlayer(player.getServer().getOfflinePlayer(TAX_ACCOUNT), taxAmount);
        if (!taxResponse.transactionSuccess()) {
            Bukkit.getLogger().warning("[Tax] 税金送金に失敗しました: " + taxResponse.errorMessage);
            player.sendMessage("税金の支払いに失敗しました。後ほど再試行してください。");
        } else {
            Bukkit.getLogger().info("[Tax] " + taxAmount + " が税金口座に送金されました。");
        }

        // オーナーへの送金
        double ownerShare = ownerAmount / regionOwners.size();  // オーナーへの分配額
        for (String owner : regionOwners) {
            EconomyResponse ownerResponse = economy.depositPlayer(player.getServer().getOfflinePlayer(owner), ownerShare);
            if (!ownerResponse.transactionSuccess()) {
                Bukkit.getLogger().warning("[Tax] オーナー " + owner + " への送金に失敗しました: " + ownerResponse.errorMessage);
                player.sendMessage("リージョン入場の税金として %.2f が税金口座に送金されました。");
            } else {
                Bukkit.getLogger().info("[Tax] " + ownerShare + " がオーナー " + owner + " に送金されました。");
                player.sendMessage(String.format("リージョンオーナー %s に %.2f が送金されました。", owner, ownerShare));
            }
        }

        // メッセージの送信
        player.sendMessage(String.format("リージョンに入るために %.2f の税金が請求されました。", taxAmount));
    }

    // 入場料に対する税金計算
    private double calculateTax(double entryFee) {
        double taxAmount = entryFee * taxRate;
        return Math.min(roundToTwoDecimalPlaces(taxAmount), entryFee);  // 税金額が支払額を超えないようにする
    }

    // 小数点以下2桁に丸める
    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // 特定のリージョンのオーナーを取得するメソッド
    private Set<String> getRegionOwnersForRegion(Player player, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            return Set.of();  // リージョンデータがロードされていない
        }

        // 指定されたリージョンを取得
        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region == null) {
            return Set.of();  // リージョンが存在しない場合
        }

        // オーナーのUUIDを取得し、名前に変換
        return region.getOwners().getUniqueIds().stream()
                .map(uuid -> player.getServer().getOfflinePlayer(uuid).getName())
                .collect(Collectors.toSet());
    }

    // リージョンの入場料を設定
    public void setRegionFee(String regionId, double amount, Player player) {
        // RegionFeeManager の setRegionFee メソッドを呼び出して設定
        regionFeeManager.setRegionFee(regionId, amount, player);  // null の代わりに player を渡す

        // regionFees にも追加
        regionFees.put(regionId, amount);
    }
}
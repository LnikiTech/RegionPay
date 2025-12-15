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
    private static final String TAX_ACCOUNT = "tax";  
    private final Map<String, Double> regionFees = new HashMap<>();  
    private final RegionFeeManager regionFeeManager;
    private final JavaPlugin plugin;  
    private File feeFile;
    private YamlConfiguration feeConfig;

    
    public RegionFeeHandler(Economy economy, double taxRate, RegionFeeManager regionFeeManager, JavaPlugin plugin) {
        this.economy = economy;
        this.taxRate = taxRate;
        this.regionFeeManager = regionFeeManager;  
        this.plugin = plugin;  

        
        this.feeFile = new File(plugin.getDataFolder(), "regionFees.yml");

        if (!feeFile.exists()) {
            try {
                feeFile.createNewFile();  
                
                feeConfig = new YamlConfiguration();
                feeConfig.set("defaultRegion", 100.0);  
                feeConfig.save(feeFile);
            } catch (IOException e) {
                plugin.getLogger().warning("料金設定ファイルを作成できませんでした！");
            }
        }

        feeConfig = YamlConfiguration.loadConfiguration(feeFile);  
        loadRegionFees();  
    }

    
    public void setTaxRate(double newTaxRate) {
        this.taxRate = newTaxRate;  
        plugin.getConfig().set("tax-rate", newTaxRate);  
        plugin.saveConfig();  

        
        plugin.getLogger().info("[RegionPay] 税率が変更されました: " + newTaxRate);
    }

    
    public double getTaxRate() {
        return taxRate;  
    }

    
    public void saveRegionFees() {
        
        for (Map.Entry<String, Double> entry : regionFees.entrySet()) {
            feeConfig.set(entry.getKey(), entry.getValue()); 
        }

        try {
            feeConfig.save(feeFile); 
        } catch (IOException e) {
            plugin.getLogger().warning("料金設定を保存できませんでした！");
        }
    }

    public void reloadConfig() {
        
        plugin.reloadConfig();  

        
        double newTaxRate = plugin.getConfig().getDouble("tax-rate", 0.1);  

        
        setTaxRate(newTaxRate);

        
        plugin.getLogger().info("新しい税率が設定されました: " + newTaxRate);
        plugin.getLogger().info("現在の設定ファイルの税率: " + plugin.getConfig().getDouble("tax-rate"));
        plugin.getLogger().info("新しい税率: " + getTaxRate());
    }

    
    public void loadRegionFees() {
        if (feeConfig == null) {
            plugin.getLogger().warning("設定ファイルが正常に読み込まれませんでした。");
            return;
        }

        for (String key : feeConfig.getKeys(false)) {
            regionFees.put(key, feeConfig.getDouble(key)); 
        }
    }

    
    public double getRegionFee(String regionName) {
        if (!regionFees.containsKey(regionName)) {
            plugin.getLogger().warning("リージョン " + regionName + " の料金が設定されていません。");
            return 0.0;  
        }
        return regionFees.get(regionName);
    }

    
    public void setRegionFee(String regionName, double feeAmount) {
        regionFees.put(regionName, feeAmount);  
        saveRegionFees();  
    }

    public boolean handleRegionFees(Player player, Set<String> regions) {
        StringBuilder message = new StringBuilder();
        boolean allFeesPaid = true; 

        for (String regionId : regions) {
            
            Set<String> regionOwners = getRegionOwnersForRegion(player, regionId);
            if (regionOwners.contains(player.getName())) {
                continue; 
            }

            
            if (isEntryFeeRequired(player, regionId)) {
                double entryFee = getEntryFeeForRegion(regionId);
                
                if (chargePlayerEntryFee(player, entryFee, regionId)) {
                    message.append(String.format("リージョン %s に入るために %.2f が請求されました。\n", regionId, entryFee));
                } else {
                    message.append("このリージョンに入るための所持金が不足しています。\n");
                    allFeesPaid = false; 
                }
            }
        }

        if (message.length() > 0) {
            player.sendMessage(message.toString());
        }

        return allFeesPaid; 
    }

    
    public boolean isEntryFeeRequired(Player player, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));  

        if (regionManager == null) {
            return false;  
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region == null) {
            return false;  
        }

        
        Flag<?> flag = Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), "regionpay-entry-fee");

        
        if (flag instanceof StateFlag) {
            StateFlag stateFlag = (StateFlag) flag;

            
            if (region.getFlag(stateFlag) == StateFlag.State.ALLOW) {
                return regionFees.containsKey(regionId);  
            }
        }

        return false;
    }

    
    private double getEntryFeeForRegion(String regionId) {
        return regionFees.getOrDefault(regionId, 0.0);  
    }

    
    private boolean chargePlayerEntryFee(Player player, double entryFee, String regionId) {
        
        Set<String> regionOwners = getRegionOwnersForRegion(player, regionId);  

        
        if (regionOwners.contains(player.getName())) {
            return true; 
        }

        
        if (economy.has(player, entryFee)) {
            economy.withdrawPlayer(player, entryFee);
            distributeFeePayment(entryFee, player, regionOwners);  
            return true;
        } else {
            
            Bukkit.getLogger().warning("[Tax] プレイヤー " + player.getName() + " の所持金が不足しています。");
            player.sendMessage("所持金が不足しているため、リージョンに入れませんでした。");

            
            return false;
        }
    }

    
    private void distributeFeePayment(double entryFee, Player player, Set<String> regionOwners) {
        double taxAmount = calculateTax(entryFee);  
        double ownerAmount = entryFee - taxAmount;

        
        EconomyResponse taxResponse = economy.depositPlayer(player.getServer().getOfflinePlayer(TAX_ACCOUNT), taxAmount);
        if (!taxResponse.transactionSuccess()) {
            Bukkit.getLogger().warning("[Tax] 税金送金に失敗しました: " + taxResponse.errorMessage);
            player.sendMessage("税金の支払いに失敗しました。後ほど再試行してください。");
        } else {
            Bukkit.getLogger().info("[Tax] " + taxAmount + " が税金口座に送金されました。");
        }

        
        double ownerShare = ownerAmount / regionOwners.size();  
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

        
        player.sendMessage(String.format("リージョンに入るために %.2f の税金が請求されました。", taxAmount));
    }

    
    private double calculateTax(double entryFee) {
        double taxAmount = entryFee * taxRate;
        return Math.min(roundToTwoDecimalPlaces(taxAmount), entryFee);  
    }

    
    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    
    private Set<String> getRegionOwnersForRegion(Player player, String regionId) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            return Set.of();  
        }

        
        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region == null) {
            return Set.of();  
        }

        
        return region.getOwners().getUniqueIds().stream()
                .map(uuid -> player.getServer().getOfflinePlayer(uuid).getName())
                .collect(Collectors.toSet());
    }

    
    public void setRegionFee(String regionId, double amount, Player player) {
        
        regionFeeManager.setRegionFee(regionId, amount, player);  

        
        regionFees.put(regionId, amount);
    }
}

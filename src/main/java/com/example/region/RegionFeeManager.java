package com.example.region;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

public class RegionFeeManager {
    private static final DoubleFlag FEE_FLAG = new DoubleFlag("fee");
    private final WorldGuardPlugin worldGuardPlugin;

    public RegionFeeManager(Plugin plugin) {
        if (plugin instanceof WorldGuardPlugin) {
            this.worldGuardPlugin = (WorldGuardPlugin) plugin;
        } else {
            throw new IllegalArgumentException("指定されたプラグインはWorldGuardPluginではありません。");
        }
    }

    
       public void setRegionFee(String regionId, double fee, Player player) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            Bukkit.getLogger().warning("ワールド " + player.getWorld().getName() + " のRegionManagerがnullです。");
            return;
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region != null) {
            region.setFlag(FEE_FLAG, fee);
            player.sendMessage("リージョン " + regionId + " の料金は " + fee + " に設定されました。");
        } else {
            player.sendMessage("リージョン " + regionId + " が現在のワールドに見つかりません。");
        }
    }

    
    public double getRegionFee(String regionId, Player player) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            Bukkit.getLogger().warning("ワールド " + player.getWorld().getName() + " のRegionManagerがnullです。");
            return 0.0;
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region != null) {
            Double feeValue = region.getFlag(FEE_FLAG);
            if (feeValue != null) {
                return feeValue;
            } else {
                player.sendMessage("リージョン " + regionId + " に料金が設定されていません。");
            }
        } else {
            player.sendMessage("リージョン " + regionId + " が現在のワールドに見つかりません。");
        }

        return 0.0;
    }

    
    public boolean isPlayerInRegion(Player player, String regionId) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            Bukkit.getLogger().warning("ワールド " + player.getWorld().getName() + " のRegionManagerがnullです。");
            return false;
        }

        ProtectedRegion region = regionManager.getRegion(regionId);
        if (region != null) {
            return region.contains(BukkitAdapter.asBlockVector(player.getLocation()));
        }

        return false;
    }

    
    public boolean isEntryFeeRequired(Player player, String regionId) {
        double fee = getRegionFee(regionId, player);
        return fee > 0.0;
    }
}

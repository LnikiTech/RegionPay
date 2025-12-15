package com.example.region;

import net.milkbowl.vault.economy.Economy;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Set;
import java.util.stream.Collectors;

public class PlayerMoveListener implements Listener {
    private final Economy economy;
    private final PlayerRegionSession regionSession;
    private final RegionFeeHandler regionFeeHandler;

    public PlayerMoveListener(Economy economy, PlayerRegionSession regionSession, RegionFeeHandler regionFeeHandler) {
        this.economy = economy;
        this.regionSession = regionSession;
        this.regionFeeHandler = regionFeeHandler;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        
        if (event.getTo() == null) {
            return;
        }

        
        String worldName = player.getWorld().getName();

        
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) {
            return;
        }

        
        Set<String> fromRegions = getRegionsForLocation(regionManager, event.getFrom());
        Set<String> toRegions = getRegionsForLocation(regionManager, event.getTo());

        
        if (fromRegions == null || toRegions == null || fromRegions.equals(toRegions)) {
            return;
        }

        
        regionSession.removeRegion(player.getUniqueId(), worldName, fromRegions);
        regionSession.addRegion(player.getUniqueId(), worldName, toRegions);

        
        boolean feesHandled = regionFeeHandler.handleRegionFees(player, toRegions);

        
        if (!feesHandled) {
            event.setCancelled(true);  
            player.teleport(event.getFrom());  
            player.sendMessage("所持金が不足しているため、リージョンに入れませんでした。");
        }
    }

    
    private Set<String> getRegionsForLocation(RegionManager regionManager, Location location) {
        
        if (regionManager == null) {
            return Set.of();  
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        if (regions == null) {
            return Set.of();  
        }

        return regions.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toSet());
    }
}

package com.example.region;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import java.util.*;

public class PlayerRegionSession {
    
    private final Map<UUID, Map<String, Set<String>>> playerRegions = new HashMap<>();
    private final RegionFeeHandler regionFeeHandler;

    
    public PlayerRegionSession(RegionFeeHandler regionFeeHandler) {
        this.regionFeeHandler = regionFeeHandler;
    }

    
    public Set<String> getRegions(UUID playerUUID, String worldName) {
        Map<String, Set<String>> worldRegions = playerRegions.get(playerUUID);
        if (worldRegions == null) {
            return Set.of();  
        }
        return worldRegions.getOrDefault(worldName, Set.of());
    }

    
    public void addRegion(UUID playerUUID, String worldName, Set<String> regions) {
        playerRegions.computeIfAbsent(playerUUID, k -> new HashMap<>())
                .computeIfAbsent(worldName, k -> new HashSet<>())
                .addAll(regions);
    }

    
    public void removeRegion(UUID playerUUID, String worldName, Set<String> regions) {
        Map<String, Set<String>> worldRegions = playerRegions.get(playerUUID);
        if (worldRegions == null) {
            return;
        }

        Set<String> currentRegions = worldRegions.get(worldName);
        if (currentRegions != null && regions != null && !regions.isEmpty()) {
            currentRegions.removeAll(regions);
            
            if (currentRegions.isEmpty()) {
                worldRegions.remove(worldName);
            }
        }

        
        if (worldRegions.isEmpty()) {
            playerRegions.remove(playerUUID);
        }
    }

    
    public boolean isFeeRequired(UUID playerUUID, String worldName) {
        Map<String, Set<String>> worldRegions = playerRegions.get(playerUUID);
        if (worldRegions == null) {
            return false;
        }

        Set<String> regions = worldRegions.get(worldName);
        if (regions == null || regions.isEmpty()) {
            return false;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) {
            return false;
        }

        for (String regionId : regions) {
            if (doesRegionRequireEntryFee(player, regionId)) {
                return true;
            }
        }
        return false;
    }

    
    private boolean doesRegionRequireEntryFee(Player player, String regionId) {
        return regionFeeHandler.isEntryFeeRequired(player, regionId);
    }
}

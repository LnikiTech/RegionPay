package com.example.region;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import java.util.*;

public class PlayerRegionSession {
    // プレイヤーUUIDをキーとして、ワールド名とリージョンの情報を保持するマップ
    private final Map<UUID, Map<String, Set<String>>> playerRegions = new HashMap<>();
    private final RegionFeeHandler regionFeeHandler;

    // コンストラクタでRegionFeeHandlerを受け取る
    public PlayerRegionSession(RegionFeeHandler regionFeeHandler) {
        this.regionFeeHandler = regionFeeHandler;
    }

    // プレイヤーのワールドごとのリージョンセッションを取得する
    public Set<String> getRegions(UUID playerUUID, String worldName) {
        Map<String, Set<String>> worldRegions = playerRegions.get(playerUUID);
        if (worldRegions == null) {
            return Set.of();  // プレイヤーがまだリージョンに参加していない場合
        }
        return worldRegions.getOrDefault(worldName, Set.of());
    }

    // プレイヤーが現在いるワールドのリージョンを追加する
    public void addRegion(UUID playerUUID, String worldName, Set<String> regions) {
        playerRegions.computeIfAbsent(playerUUID, k -> new HashMap<>())
                .computeIfAbsent(worldName, k -> new HashSet<>())
                .addAll(regions);
    }

    // プレイヤーが現在いるワールドのリージョンを削除する
    public void removeRegion(UUID playerUUID, String worldName, Set<String> regions) {
        Map<String, Set<String>> worldRegions = playerRegions.get(playerUUID);
        if (worldRegions == null) {
            return;
        }

        Set<String> currentRegions = worldRegions.get(worldName);
        if (currentRegions != null && regions != null && !regions.isEmpty()) {
            currentRegions.removeAll(regions);
            // リージョンが空になった場合は、ワールドごと削除
            if (currentRegions.isEmpty()) {
                worldRegions.remove(worldName);
            }
        }

        // プレイヤーのリージョンマップが空なら削除
        if (worldRegions.isEmpty()) {
            playerRegions.remove(playerUUID);
        }
    }

    // プレイヤーが現在いるワールドのリージョンに入場料が発生するかを確認する
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

    // 特定のリージョンに入場料が必要かどうかを確認
    private boolean doesRegionRequireEntryFee(Player player, String regionId) {
        return regionFeeHandler.isEntryFeeRequired(player, regionId);
    }
}
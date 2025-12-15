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

        // 移動先がnullの場合は処理を終了
        if (event.getTo() == null) {
            return;
        }

        // ワールド名を取得
        String worldName = player.getWorld().getName();

        // ワールドガードのリージョンマネージャーを取得
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) {
            return;
        }

        // 移動前と移動後のリージョンを取得
        Set<String> fromRegions = getRegionsForLocation(regionManager, event.getFrom());
        Set<String> toRegions = getRegionsForLocation(regionManager, event.getTo());

        // リージョンが変わらない場合は処理を終了
        if (fromRegions == null || toRegions == null || fromRegions.equals(toRegions)) {
            return;
        }

        // プレイヤーのリージョンを更新
        regionSession.removeRegion(player.getUniqueId(), worldName, fromRegions);
        regionSession.addRegion(player.getUniqueId(), worldName, toRegions);

        // 移動後のリージョンの料金を処理
        boolean feesHandled = regionFeeHandler.handleRegionFees(player, toRegions);

        // 料金の支払いが正常に行われなかった場合、移動をキャンセルし、元の位置に戻す
        if (!feesHandled) {
            event.setCancelled(true);  // 料金が支払われなかった場合、移動をキャンセル
            player.teleport(event.getFrom());  // プレイヤーを元の位置に戻す
            player.sendMessage("所持金が不足しているため、リージョンに入れませんでした。");
        }
    }

    // 指定した座標のリージョンを取得
    private Set<String> getRegionsForLocation(RegionManager regionManager, Location location) {
        // エラーハンドリングを追加
        if (regionManager == null) {
            return Set.of();  // regionManagerがnullの場合、空のセットを返す
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        if (regions == null) {
            return Set.of();  // regionsがnullの場合、空のセットを返す
        }

        return regions.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toSet());
    }
}
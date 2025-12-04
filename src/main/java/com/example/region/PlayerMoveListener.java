package com.example.region;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.World;

public class PlayerMoveListener implements Listener {
    private final Economy economy;
    private static double TAX_RATE;
    private static final String TAX_ACCOUNT = "tax";

    private static final DoubleFlag ENTRY_FEE = new DoubleFlag("entry-fee");

    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public PlayerMoveListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.economy = getEconomy();
        if (this.economy == null) {
            Bukkit.getLogger().warning("[RegionPay] Vault経済プラグインが見つかりません。");
        }

        TAX_RATE = loadTaxRate();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) return;

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(event.getTo()));

        for (ProtectedRegion region : regions) {
            Double entryFee = region.getFlag(ENTRY_FEE);
            if (entryFee != null && entryFee > 0) {
                chargePlayer(player, region, entryFee);
                return;
            }
        }
    }

    private void chargePlayer(Player player, ProtectedRegion region, double fee) {
        if (economy == null) return;

        if (economy.has(player, fee)) {
            economy.withdrawPlayer(player, fee);
            player.sendMessage(String.format("You have been charged %.2f for entering the region.", fee));
            distributePayment(region, fee);
        } else {
            player.sendMessage("You do not have enough money to enter this region.");
        }
    }

    private void distributePayment(ProtectedRegion region, double fee) {
        if (economy == null) return;

        double taxAmount = fee * TAX_RATE;
        double ownerAmount = fee - taxAmount;

        OfflinePlayer taxAccount = Bukkit.getOfflinePlayer(TAX_ACCOUNT);
        economy.depositPlayer(taxAccount, taxAmount);

        String ownerId = getRegionOwner(region.getId());
        if (ownerId != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
            economy.depositPlayer(owner, ownerAmount);
            if (owner.isOnline()) {
                owner.getPlayer().sendMessage(String.format("You have received %.2f for someone entering your region.", ownerAmount));
            }
        }
    }

    private String getRegionOwner(String regionId) {
        for (World world : Bukkit.getWorlds()) {
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(regionId);
                if (region != null) {
                    String ownerId = region.getOwners().getPlayers().isEmpty() ? null : region.getOwners().getPlayers().iterator().next();
                    if (ownerId != null) {
                        return ownerId;
                    }
                }
            }
        }
        return null;
    }

    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        return (rsp != null) ? rsp.getProvider() : null;
    }

    private double loadTaxRate() {
        return config.getDouble("tax-rate", 0.1);
    }

    public void reloadTaxRate() {
        TAX_RATE = loadTaxRate();
        Bukkit.getLogger().info("[RegionPay] 税率が再読み込みされました: " + TAX_RATE);
    }

    public static double getTaxRate() {
        return TAX_RATE;
    }
}
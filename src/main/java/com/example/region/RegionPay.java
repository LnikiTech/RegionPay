package com.example.region;

import org.bukkit.plugin.java.JavaPlugin;

public class RegionPay extends JavaPlugin {

    private PlayerMoveListener playerMoveListener;
    private ReloadCommand reloadCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerMoveListener = new PlayerMoveListener(this);
        this.reloadCommand = new ReloadCommand(playerMoveListener);

        getCommand("regionpay").setExecutor(reloadCommand);

        getLogger().info("RegionPay plugin is enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RegionPay plugin is disabled!");
    }
}
package com.example.region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class ReloadCommand implements CommandExecutor {

    private final PlayerMoveListener playerMoveListener;

    public ReloadCommand(PlayerMoveListener playerMoveListener) {
        this.playerMoveListener = playerMoveListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("regionpay")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                playerMoveListener.reloadTaxRate();
                sender.sendMessage("税率がリロードされました。");
                return true;
            }
        }
        return false;
    }
}
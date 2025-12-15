package com.example.region;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final RegionPlugin plugin;
    private final RegionFeeHandler regionFeeHandler;

    public ReloadCommand(RegionPlugin plugin, RegionFeeHandler regionFeeHandler) {
        this.plugin = plugin;
        this.regionFeeHandler = regionFeeHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sender.sendMessage("Usage: /regionpay <reload|setfee>");
            return false;
        }

        
        if (args[0].equalsIgnoreCase("reload")) {
            
            plugin.reloadPlugin();  

        
            sender.sendMessage("RegionPayの設定と税率がリロードされました！ 新しい税率: " + plugin.getFeeHandler().getTaxRate());
            return true;
        }

        
        if (args[0].equalsIgnoreCase("setfee")) {
            
            if (args.length != 3) {
                sender.sendMessage("Usage: /regionpay setfee <region> <fee>");
                return false;
            }

            String regionName = args[1];  
            double feeAmount;

            
            try {
                feeAmount = Double.parseDouble(args[2]);  
            } catch (NumberFormatException e) {
                sender.sendMessage("無効な金額です。正しい数値を入力してください。");
                return false;
            }

            
            if (feeAmount < 0) {
                sender.sendMessage("料金は0以上である必要があります。");
                return false;
            }

            
            if (!(sender instanceof Player)) {
                sender.sendMessage("このコマンドはプレイヤーだけが実行できます。");
                return false;
            }

            
            Player player = (Player) sender;
            if (!player.hasPermission("regionpay.setfee")) {  
                player.sendMessage("あなたにはこのコマンドを実行する権限がありません。");
                return false;
            }

            
            regionFeeHandler.setRegionFee(regionName, feeAmount);
            sender.sendMessage("リージョン " + regionName + " の入場料金が " + feeAmount + " に設定されました。");
            return true;
        }

        
        sender.sendMessage("不明なコマンドです。 Usage: /regionpay <reload|setfee>");
        return false;
    }
}

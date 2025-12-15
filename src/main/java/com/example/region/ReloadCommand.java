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
        // 引数がない場合は /regionpay reload の使い方を案内
        if (args.length == 0) {
            sender.sendMessage("Usage: /regionpay <reload|setfee>");
            return false;
        }

        // /regionpay reload コマンド
        if (args[0].equalsIgnoreCase("reload")) {
            // 設定のリロード処理
            plugin.reloadPlugin();  // plugin.reloadPlugin() を呼び出して設定をリロード

        // リロード後の税率表示
            sender.sendMessage("RegionPayの設定と税率がリロードされました！ 新しい税率: " + plugin.getFeeHandler().getTaxRate());
            return true;
        }

        // /regionpay setfee <region> <fee> コマンド
        if (args[0].equalsIgnoreCase("setfee")) {
            // 引数の長さを確認
            if (args.length != 3) {
                sender.sendMessage("Usage: /regionpay setfee <region> <fee>");
                return false;
            }

            String regionName = args[1];  // 第2引数がリージョン名
            double feeAmount;

            // 金額が正しいかチェック
            try {
                feeAmount = Double.parseDouble(args[2]);  // 第3引数が金額
            } catch (NumberFormatException e) {
                sender.sendMessage("無効な金額です。正しい数値を入力してください。");
                return false;
            }

            // 金額が0未満の場合はエラーメッセージ
            if (feeAmount < 0) {
                sender.sendMessage("料金は0以上である必要があります。");
                return false;
            }

            // プレイヤー権限チェック（オプション）
            if (!(sender instanceof Player)) {
                sender.sendMessage("このコマンドはプレイヤーだけが実行できます。");
                return false;
            }

            // プレイヤーがこのコマンドを実行できるかチェック（例: 権限確認）
            Player player = (Player) sender;
            if (!player.hasPermission("regionpay.setfee")) {  // 権限が必要
                player.sendMessage("あなたにはこのコマンドを実行する権限がありません。");
                return false;
            }

            // RegionFeeHandler の setRegionFee を呼び出して料金を設定
            regionFeeHandler.setRegionFee(regionName, feeAmount);
            sender.sendMessage("リージョン " + regionName + " の入場料金が " + feeAmount + " に設定されました。");
            return true;
        }

        // 未知のコマンドの場合
        sender.sendMessage("不明なコマンドです。 Usage: /regionpay <reload|setfee>");
        return false;
    }
}
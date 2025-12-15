package com.example.region;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import org.bukkit.Bukkit;

public class CustomFlagHandler {

    public static StateFlag ENTRY_FEE;
    public static StateFlag EXIT_FEE;

    // registerFlags メソッドを静的メソッドとして定義
    public static void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

        try {
            // "regionpay-entry-fee" フラグの作成
            if (registry.get("regionpay-entry-fee") == null) {
                ENTRY_FEE = new StateFlag("regionpay-entry-fee", true);
                registry.register(ENTRY_FEE);
            } else if (registry.get("regionpay-entry-fee") instanceof StateFlag) {
                ENTRY_FEE = (StateFlag) registry.get("regionpay-entry-fee");
            } else {
                Bukkit.getLogger().warning("[RegionPay] 'regionpay-entry-fee' のフラグが既に登録されていますが、型が異なります。");
            }

            // "regionpay-exit-fee" フラグの作成
            if (registry.get("regionpay-exit-fee") == null) {
                EXIT_FEE = new StateFlag("regionpay-exit-fee", true);
                registry.register(EXIT_FEE);
            } else if (registry.get("regionpay-exit-fee") instanceof StateFlag) {
                EXIT_FEE = (StateFlag) registry.get("regionpay-exit-fee");
            } else {
                Bukkit.getLogger().warning("[RegionPay] 'regionpay-exit-fee' のフラグが既に登録されていますが、型が異なります。");
            }

            Bukkit.getLogger().info("[RegionPay] WorldGuardにENTRY_FEEとEXIT_FEEのフラグが登録されました。");

        } catch (FlagConflictException e) {
            // フラグの競合処理
            Bukkit.getLogger().warning("[RegionPay] フラグ登録時に競合が発生しました: " + e.getMessage());
        } catch (Exception e) {
            // その他のエラー処理
            Bukkit.getLogger().warning("[RegionPay] フラグ登録時に予期しないエラーが発生しました: " + e.getMessage());
        }
    }
}
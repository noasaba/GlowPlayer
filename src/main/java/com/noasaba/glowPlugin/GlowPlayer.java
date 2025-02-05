package com.noasaba.glowplayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlowPlayer extends JavaPlugin implements Listener {

    // セッション中の各プレイヤーのグローイング状態を記録するマップ
    private final Map<UUID, Boolean> glowingStates = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("GlowPlayerが有効化されました！");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("GlowPlayerが無効化されました！");
    }

    /**
     * プレイヤー参加時のイベント
     * 登録がなければ、全員をデフォルトで光らせる（true）ように設定する
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean glowing = glowingStates.getOrDefault(uuid, true); // デフォルトは true
        glowingStates.put(uuid, glowing);
        player.setGlowing(glowing);
    }

    /**
     * プレイヤーリスポーン時のイベント
     * リスポーン直後はエンティティの初期化があるため、1tick遅延して設定する
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean glowing = glowingStates.getOrDefault(uuid, true); // デフォルトは true
        glowingStates.put(uuid, glowing);
        final boolean finalGlowing = glowing;
        Bukkit.getScheduler().runTaskLater(this, () -> player.setGlowing(finalGlowing), 1L);
    }

    /**
     * コマンド処理
     * コマンド「/glow」でグローイングのオンオフを切り替える
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("glow")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
                return true;
            }
            Player player = (Player) sender;
            // コマンド実行権限の確認（切り替え用パーミッション）
            if (!player.hasPermission("glowplayer.toggle")) {
                player.sendMessage(ChatColor.RED + "このコマンドを使用する権限がありません。");
                return true;
            }
            UUID uuid = player.getUniqueId();
            boolean currentState = glowingStates.getOrDefault(uuid, true);
            boolean newState = !currentState;
            glowingStates.put(uuid, newState);
            player.setGlowing(newState);
            player.sendMessage(ChatColor.GREEN + "グローイングが " + (newState ? "オン" : "オフ") + " になりました。");
            return true;
        }
        return false;
    }
}

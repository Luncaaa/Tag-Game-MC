package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.StatsManager;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlayersManager {
    private final TagGame plugin;
    private final Map<String, PlayerData> playersData = new HashMap<>();

    public PlayersManager(TagGame plugin) {
        this.plugin = plugin;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        playersData.put(player.getName(), new PlayerData(plugin, player));
    }

    public void removePlayer(String playerName) {
        PlayerData playerData = playersData.get(playerName);
        if (playerData.isInArena()) playerData.arena.playerLeave(playerData.getPlayer(), true);
        if (playerData.isSettingUpArena() || playerData.isInArena()) playerData.saveData();
        playerData.getStatsManager().saveData(true);
        playersData.remove(playerName);
    }

    // Used for the onDisable method. If in-game or setting up an arena when plugin is disabled, it restores the inventories.
    public CompletableFuture<Void> removeEveryone() {
        return CompletableFuture.runAsync(() -> {
            for (PlayerData playerData : playersData.values()) {
                if (playerData.isSettingUpArena() || playerData.isInArena()) playerData.restoreSavedData();
                playerData.getStatsManager().saveData(false);
            }
            playersData.clear();
        });
    }

    public StatsManager getPlayerStats(OfflinePlayer player) {
        if (playersData.containsKey(player.getName())) {
            return playersData.get(player.getName()).getStatsManager();
        } else {
            me.lucaaa.tag.managers.StatsManager stats = new me.lucaaa.tag.managers.StatsManager(player, plugin, true);
            return (stats.isPlayerSaved()) ? stats : null;
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playersData.computeIfAbsent(player.getName(), p -> new PlayerData(plugin, player));
    }
}
package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.StatsManager;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class PlayersManager {
    private final TagGame plugin;
    private final HashMap<String, PlayerData> playersData = new HashMap<>();

    public PlayersManager(TagGame plugin) {
        this.plugin = plugin;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        this.playersData.put(player.getName(), new PlayerData(plugin, player));
    }

    public void removePlayer(String playerName) {
        PlayerData playerData = this.playersData.get(playerName);
        if (playerData.isInArena()) playerData.arena.playerLeave(playerData.getPlayer(), true);
        if (playerData.isSettingUpArena() || playerData.isInArena()) playerData.saveData();
        playerData.getStatsManager().saveData(true);
        this.playersData.remove(playerName);
    }

    // Used for the onDisable method. If in-game or setting up an arena when plugin is disabled, it restores the inventories.
    public CompletableFuture<Void> removeEveryone() {
        return CompletableFuture.runAsync(() -> {
            for (PlayerData playerData : this.playersData.values()) {
                if (playerData.isSettingUpArena() || playerData.isInArena()) playerData.restoreSavedData();
                playerData.getStatsManager().saveData(false);
            }
            this.playersData.clear();
        });
    }

    public StatsManager getPlayerStats(String playerName) {
        if (this.playersData.containsKey(playerName)) {
            return this.playersData.get(playerName).getStatsManager();
        } else {
            return new me.lucaaa.tag.managers.StatsManager(playerName, plugin);
        }
    }

    public PlayerData getPlayerData(Player player) {
        return this.playersData.computeIfAbsent(player.getName(), p -> new PlayerData(plugin, player));
    }
}
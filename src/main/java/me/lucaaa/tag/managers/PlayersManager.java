package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.TagPlayer;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

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
        if (!plugin.getDatabaseManager().playerIsInDB(player.getName())) {
            plugin.getDatabaseManager().createPlayer(player.getName());
        }
    }

    public void removePlayer(String playerName) {
        PlayerData playerData = this.playersData.get(playerName);
        if (playerData.isInArena()) playerData.arena.playerLeave(playerData.getPlayer(), true);
        if (playerData.isSettingUpArena() || playerData.isInArena()) playerData.saveData();
        this.playersData.remove(playerName);
    }

    // Used for the onDisable method. If in-game or setting up an arena when plugin is disabled, it restores the inventories.
    public void removeEveryone() {
        for (PlayerData playerData : this.playersData.values()) {
            if (playerData.isSettingUpArena() || playerData.isInArena()) playerData.restoreSavedData();
        }
        this.playersData.clear();
    }

    public PlayerData getPlayerData(String playerName) {
        return this.playersData.get(playerName);
    }

    public TagPlayer getTagPlayer(String playerName) {
        return this.playersData.get(playerName);
    }
}
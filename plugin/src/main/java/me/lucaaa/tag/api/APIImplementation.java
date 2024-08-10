package me.lucaaa.tag.api;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.StatsManager;
import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class APIImplementation implements TagAPI {
    private final TagGame plugin;

    public APIImplementation(TagGame plugin) {
        this.plugin = plugin;
    }

    @Override
    public TagPlayer getTagPlayer(Player player) {
        return plugin.getPlayersManager().getPlayerData(player);
    }

    @Override
    public TagArena getTagArena(String arenaName) {
        return plugin.getArenasManager().getTagArena(arenaName);
    }

    @Override
    public StatsManager getPlayerStats(OfflinePlayer player) {
        return plugin.getPlayersManager().getPlayerStats(player);
    }
}
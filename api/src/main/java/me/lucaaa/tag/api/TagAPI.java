package me.lucaaa.tag.api;

import me.lucaaa.tag.api.game.StatsManager;
import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * TagGame's API
 */
@SuppressWarnings("unused")
public interface TagAPI {
    /**
     * Gets the TagGame API.
     * @return The TagGame API.
     */
    static TagAPI get() {
        return APIProvider.getImplementation();
    }

    /**
     * Gets information about a player.
     *
     * @param player The player you want to get information of.
     * @return The player you want or null if it was not found.
     */
    TagPlayer getTagPlayer(Player player);

    /**
     * Gets an arena.
     *
     * @param arenaName The name of the arena you want to get.
     * @return The arena you want or null if it was not found.
     */
    TagArena getTagArena(String arenaName);

    /**
     * Gets the plugin's stats for a player.
     *
     * <p>It is recommended that if the player is online you use {@link TagPlayer#getStatsManager()} instead.<br>
     * If the player is not in the database, it will return null.<br>
     * You should run this method asynchronously.</p>
     *
     * @param player The player's stats you want to get.
     * @return The player's stats
     */
    StatsManager getPlayerStats(OfflinePlayer player);
}
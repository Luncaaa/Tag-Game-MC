package me.lucaaa.tag.api;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
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
}
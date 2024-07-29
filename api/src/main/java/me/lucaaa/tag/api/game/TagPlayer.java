package me.lucaaa.tag.api.game;

import org.bukkit.entity.Player;

/**
 * Stores TagGame data for a player.
 */
@SuppressWarnings("unused")
public interface TagPlayer {
    /**
     * Gets the Spigot player.
     *
     * @return The Spigot player.
     */
    Player getPlayer();

    /**
     * Whether the player is setting up an arena or not.
     *
     * @return Whether the player is setting up an arena or not.
     */
    boolean isSettingUpArena();

    /**
     * Gets the arena a player is setting up.
     *
     * @return The arena that the player is setting up or null if he isn't setting up an arena.
     */
    TagArena getSettingUpArena();

    /**
     * Whether the player is in an arena or not.<br>
     * WARNING - Returns true even if he is in the waiting area.
     *
     * @return Whether the player is in an arena or not.
     */
    boolean isInArena();

    /**
     * Gets the arena a player is in.<br>
     * WARNING - Returns an arena even if he is in the waiting area.
     *
     * @return The arena that the player is playing in or null if he is not in an arena.
     */
    TagArena getArena();

    /**
     * Gets the stats manager for this player.
     * @return The stats manager.
     */
    StatsManager getStatsManager();
}
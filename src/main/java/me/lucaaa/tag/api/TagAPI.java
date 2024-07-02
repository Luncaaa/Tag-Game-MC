package me.lucaaa.tag.api;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;

@SuppressWarnings("unused")
public class TagAPI {
    private static TagGame plugin;

    /**
     * INTERNAL USE ONLY
     *
     * @hidden
     * @param tagPlugin Tag plugin
     */
    public static void setPlugin(TagGame tagPlugin) {
        plugin = tagPlugin;
    }

    /**
     * Gets information about a player.
     *
     * @param playerName The name of the player you want to get information of.
     * @return The player you want or null if it was not found.
     */
    public static TagPlayer getTagPlayer(String playerName) {
        return plugin.getPlayersManager().getTagPlayer(playerName);
    }

    /**
     * Gets an arena.
     *
     * @param arenaName The name of the arena you want to get.
     * @return The arena you want or null if it was not found.
     */
    public static TagArena getTagArena(String arenaName) {
        return plugin.getArenasManager().getTagArena(arenaName);
    }
}
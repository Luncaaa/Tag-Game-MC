package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;

/** Called when a player leaves an arena. */
@SuppressWarnings("unused")
public class TagPlayerLeaveEvent extends TagEvent {
    private final TagPlayer player;
    private final TagArena arena;

    /**
     * Constructor for this event. Internal use only.
     * @param player The player who left.
     * @param arena The arena the player left.
     */
    public TagPlayerLeaveEvent(TagPlayer player, TagArena arena) {
        this.player = player;
        this.arena = arena;
    }

    /**
     * Gets the tag player involved in this event.
     *
     * @return The tag player involved in this event.
     */
    public TagPlayer getTagPlayer() {
        return this.player;
    }

    /**
     * Gets the Spigot player involved in this event.
     *
     * @return The Spigot player involved in this event.
     */
    public Player getPlayer() {
        return this.player.getPlayer();
    }

    /**
     * Gets the arena involved in this event.
     *
     * @return The arena involved in this event.
     */
    public TagArena getArena() {
        return this.arena;
    }
}
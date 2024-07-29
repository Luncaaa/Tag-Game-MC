package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;

/** Called when a player leaves an arena. */
@SuppressWarnings("unused")
public abstract class TagPlayerLeaveEvent extends TagEvent {
    /**
     * Gets the tag player involved in this event.
     *
     * @return The tag player involved in this event.
     */
    public abstract TagPlayer getTagPlayer();

    /**
     * Gets the Spigot player involved in this event.
     *
     * @return The Spigot player involved in this event.
     */
    public abstract Player getPlayer();

    /**
     * Gets the arena involved in this event.
     *
     * @return The arena involved in this event.
     */
    public abstract TagArena getArena();
}
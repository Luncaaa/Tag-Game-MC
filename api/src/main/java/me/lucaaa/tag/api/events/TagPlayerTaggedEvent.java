package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;

/**
 * Called when a player is tagged.
 * WARNING! It Does not include initial taggers (selected when the game starts)
 */
@SuppressWarnings("unused")
public abstract class TagPlayerTaggedEvent extends CancellableTagEvent {
    /**
     * Gets the tag tagged player involved in this event.
     *
     * @return The tag tagged player involved in this event.
     */
    public abstract TagPlayer getTagPlayer();

    /**
     * Gets the Spigot tagged player involved in this event.
     *
     * @return The Spigot tagged player involved in this event.
     */
    public abstract Player getPlayer();

    /**
     * Gets the tag tagger player involved in this event.
     *
     * @return The tag tagger player involved in this event.
     */
    public abstract TagPlayer getTagTagger();

    /**
     * Gets the Spigot tagger player involved in this event.
     *
     * @return The Spigot tagger player involved in this event.
     */
    public abstract Player getTagger();

    /**
     * Gets the arena involved in this event.
     *
     * @return The arena involved in this event.
     */
    public abstract TagArena getArena();
}
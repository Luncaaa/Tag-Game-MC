package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;

/**
 * Called when a player is tagged.
 * WARNING! It Does not include initial taggers (selected when the game starts)
 */
@SuppressWarnings("unused")
public class TagPlayerTaggedEvent extends CancellableTagEvent {
    private final TagPlayer tagged;
    private final TagPlayer tagger;
    private final TagArena arena;

    /**
     * Constructor for this event. Internal use only.
     * @param tagged The player who was tagged.
     * @param tagger The player who tagged another player.
     * @param arena The arena where this occurred.
     */
    public TagPlayerTaggedEvent(TagPlayer tagged, TagPlayer tagger, TagArena arena) {
        this.tagged = tagged;
        this.tagger = tagger;
        this.arena = arena;
    }

    /**
     * Gets the tag tagged player involved in this event.
     *
     * @return The tag tagged player involved in this event.
     */
    public TagPlayer getTagPlayer() {
        return this.tagged;
    }

    /**
     * Gets the Spigot tagged player involved in this event.
     *
     * @return The Spigot tagged player involved in this event.
     */
    public Player getPlayer() {
        return this.tagged.getPlayer();
    }

    /**
     * Gets the tag tagger player involved in this event.
     *
     * @return The tag tagger player involved in this event.
     */
    public TagPlayer getTagTagger() {
        return this.tagger;
    }

    /**
     * Gets the Spigot tagger player involved in this event.
     *
     * @return The Spigot tagger player involved in this event.
     */
    public Player getTagger() {
        return this.tagger.getPlayer();
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
package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is tagged.
 * WARNING! It Does not include initial taggers (selected when the game starts)
 */
@SuppressWarnings("unused")
public class TagPlayerTaggedEvent extends Event implements Cancellable {
    private final PlayerData tagged;
    private final PlayerData tagger;
    private final TagArena arena;
    private static final HandlerList handlerList = new HandlerList();
    private boolean isCancelled = false;

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public TagPlayerTaggedEvent(PlayerData tagged, PlayerData tagger, TagArena arena) {
        this.tagged = tagged;
        this.tagger = tagger;
        this.arena = arena;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        if (cancelled) {
            this.tagged.clearData();
            this.tagger.clearData();
        }
        this.isCancelled = cancelled;
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
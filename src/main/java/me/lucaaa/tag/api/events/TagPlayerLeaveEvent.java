package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Called when a player leaves an arena. */
public class TagPlayerLeaveEvent extends Event {
    private final TagPlayer player;
    private final TagArena arena;
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public TagPlayerLeaveEvent(TagPlayer player, TagArena arena) {
        this.player = player;
        this.arena = arena;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
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
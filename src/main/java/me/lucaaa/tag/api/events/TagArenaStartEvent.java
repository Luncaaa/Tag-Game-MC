package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Called when an arena starts (tagger countdown starts) */
public class TagArenaStartEvent extends Event {
    private final TagArena arena;
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public TagArenaStartEvent(TagArena arena) {
        this.arena = arena;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
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
package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.utils.StopCause;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** Called when an arena stops. */
public class TagArenaStopEvent extends Event {
    private final TagArena arena;
    private final StopCause cause;
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public TagArenaStopEvent(TagArena arena, StopCause cause) {
        this.arena = arena;
        this.cause = cause;
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
    /**
     * Gets the reason why the arena was stopped. <br>
     * RELOAD: The arena was stopped because the plugin reloaded.<br>
     * COMMAND: The arena was stopped because the /tag stop [arena] command was executed.<br>
     * GAME: The arena stopped because there were not enough players to continue or the time ended.<br>
     * API: The arena stopped because the API called the event.
     *
     * @return The reason why the arena was stopped.
     */
    public StopCause getCause() {
        return this.cause;
    }
}
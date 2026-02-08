package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.enums.StopCause;
import me.lucaaa.tag.api.game.TagArena;

/** Called when an arena stops. */
@SuppressWarnings("unused")
public class TagArenaStopEvent extends TagEvent {
    private final TagArena arena;
    private final StopCause cause;

    /**
     * Constructor for this event. Internal use only.
     * @param arena - The arena
     * @param cause - What caused the game to be stopped.
     */
    public TagArenaStopEvent(TagArena arena, StopCause cause) {
        this.arena = arena;
        this.cause = cause;
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
     * Gets the reason why the arena was stopped.
     *
     * @return The reason why the arena was stopped.
     */
    public StopCause getCause() {
        return this.cause;
    }
}
package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.enums.StopCause;
import me.lucaaa.tag.api.game.TagArena;

/** Called when an arena stops. */
@SuppressWarnings("unused")
public abstract class TagArenaStopEvent extends TagEvent {
    /**
     * Gets the arena involved in this event.
     *
     * @return The arena involved in this event.
     */
    public abstract TagArena getArena();

    /**
     * Gets the reason why the arena was stopped.
     *
     * @return The reason why the arena was stopped.
     */
    public abstract StopCause getCause();
}
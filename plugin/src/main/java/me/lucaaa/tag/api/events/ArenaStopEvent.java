package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.enums.StopCause;
import me.lucaaa.tag.api.game.TagArena;

/** Called when an arena stops. */
@SuppressWarnings("unused")
public class ArenaStopEvent extends TagArenaStopEvent {
    private final TagArena arena;
    private final StopCause cause;

    public ArenaStopEvent(TagArena arena, StopCause cause) {
        this.arena = arena;
        this.cause = cause;
    }

    @Override
    public TagArena getArena() {
        return this.arena;
    }

    @Override
    public StopCause getCause() {
        return this.cause;
    }
}
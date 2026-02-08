package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;

/** Called when an arena starts (tagger countdown starts) */
public class TagArenaStartEvent extends TagEvent {
    private final TagArena arena;

    /**
     * Constructor for this event. Internal use only.
     * @param arena - The arena
     */
    public TagArenaStartEvent(TagArena arena) {
        this.arena = arena;
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
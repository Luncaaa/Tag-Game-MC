package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;

/** Called when an arena starts (tagger countdown starts) */
@SuppressWarnings("unused")
public abstract class TagArenaStartEvent extends TagEvent {
    /**
     * Gets the arena involved in this event.
     *
     * @return The arena involved in this event.
     */
    public abstract TagArena getArena();
}
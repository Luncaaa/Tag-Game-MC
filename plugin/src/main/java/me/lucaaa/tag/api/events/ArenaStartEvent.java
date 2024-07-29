package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;

/** Called when an arena starts (tagger countdown starts) */
@SuppressWarnings("unused")
public class ArenaStartEvent extends TagArenaStartEvent {
    private final TagArena arena;

    public ArenaStartEvent(TagArena arena) {
        this.arena = arena;
    }

    @Override
    public TagArena getArena() {
        return this.arena;
    }
}
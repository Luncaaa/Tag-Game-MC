package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;

/**
 * Called when a player is tagged.
 * WARNING! It Does not include initial taggers (selected when the game starts)
 */
@SuppressWarnings("unused")
public class PlayerTaggedEvent extends TagPlayerTaggedEvent {
    private final TagPlayer tagged;
    private final TagPlayer tagger;
    private final TagArena arena;

    public PlayerTaggedEvent(TagPlayer tagged, TagPlayer tagger, TagArena arena) {
        this.tagged = tagged;
        this.tagger = tagger;
        this.arena = arena;
    }

    @Override
    public TagPlayer getTagPlayer() {
        return this.tagged;
    }

    @Override
    public Player getPlayer() {
        return this.tagged.getPlayer();
    }

    @Override
    public TagPlayer getTagTagger() {
        return this.tagger;
    }

    @Override
    public Player getTagger() {
        return this.tagger.getPlayer();
    }

    @Override
    public TagArena getArena() {
        return this.arena;
    }
}
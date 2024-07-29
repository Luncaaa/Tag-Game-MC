package me.lucaaa.tag.api.events;

import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import org.bukkit.entity.Player;

/** Called when a player joins an arena. */
@SuppressWarnings("unused")
public class PlayerJoinEvent extends TagPlayerJoinEvent {
    private final TagPlayer player;
    private final TagArena arena;

    public PlayerJoinEvent(TagPlayer player, TagArena arena) {
        this.player = player;
        this.arena = arena;
    }

    @Override
    public TagPlayer getTagPlayer() {
        return this.player;
    }

    @Override
    public Player getPlayer() {
        return this.player.getPlayer();
    }

    @Override
    public TagArena getArena() {
        return this.arena;
    }
}
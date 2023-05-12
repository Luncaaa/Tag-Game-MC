package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Objects;

public class SignChangeListener implements Listener {
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission("tag.admin") && !event.getPlayer().hasPermission("tag.setup")) return;
        if (event.getLine(0) == null || event.getLine(1) == null) return;
        if (!Objects.equals(event.getLine(0), "[TAG]")) return;
        if (!TagGame.arenasManager.arenas.containsKey(event.getLine(1))) return;

        Arena arena = TagGame.arenasManager.getArena(event.getLine(1));
        arena.addSign(event.getBlock().getLocation());
        for (int index = 0; index < event.getLines().length; index++) {
            event.setLine(index, TagGame.messagesManager.getMessageFromList("signs", index, arena.getPlaceholders(), null));
        }
    }
}
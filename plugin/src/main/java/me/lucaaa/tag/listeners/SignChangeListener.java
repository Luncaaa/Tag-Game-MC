package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Objects;

public class SignChangeListener implements Listener {
    private final TagGame plugin;

    public SignChangeListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0) == null || event.getLine(1) == null) return;
        if (!Objects.equals(event.getLine(0), "[TAG]")) return;
        if (!plugin.getArenasManager().arenas.containsKey(event.getLine(1))) return;

        Arena arena = plugin.getArenasManager().getArena(event.getLine(1));

        if (!event.getPlayer().hasPermission("tag.admin") && !event.getPlayer().hasPermission("tag.setup")) {
            plugin.getMessagesManager().sendMessage("joinSigns.no-permission", arena.getPlaceholders(), event.getPlayer());
            event.setCancelled(true);
            return;
        }

        arena.addSign(event.getBlock().getLocation());
        for (int index = 0; index < event.getLines().length; index++) {
            event.setLine(index, plugin.getMessagesManager().getMessageFromList("signs", index, arena.getPlaceholders(), null));
        }

        plugin.getMessagesManager().sendMessage("joinSigns.created", arena.getPlaceholders(), event.getPlayer());
    }
}
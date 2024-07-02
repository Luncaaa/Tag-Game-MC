package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final TagGame plugin;

    public PlayerQuitListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayersManager().removePlayer(event.getPlayer().getName());
    }
}
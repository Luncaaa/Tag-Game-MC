package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final TagGame plugin;

    public PlayerJoinListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPlayersManager().addPlayer(event.getPlayer());
    }
}
package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        TagGame.playersManager.addPlayer(event.getPlayer());
    }
}
package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        TagGame.playersManager.removePlayer(event.getPlayer().getName());
    }
}
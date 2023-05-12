package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListener implements Listener {
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        PlayerData playerData = TagGame.playersManager.getPlayerData(event.getPlayer().getName());
        if (playerData.isInArena() || playerData.isSettingUpArena()) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        PlayerData playerData = TagGame.playersManager.getPlayerData(event.getWhoClicked().getName());
        if (playerData.isInArena() || playerData.isSettingUpArena()) event.setCancelled(true);
    }
}
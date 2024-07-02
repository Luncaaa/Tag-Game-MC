package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListener implements Listener {
    private final TagGame plugin;

    public InventoryListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(event.getPlayer().getName());
        if (playerData.isInArena() || playerData.isSettingUpArena()) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(event.getWhoClicked().getName());
        if (playerData.isInArena() || playerData.isSettingUpArena()) event.setCancelled(true);
    }
}
package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class BlockBreakListener implements Listener {
    private final TagGame plugin;

    public BlockBreakListener(TagGame plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player.getName());
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // If the block that was broken was a join sign, remove it from the arena's signs, and the signs list.
        if (!playerData.isSettingUpArena() && !playerData.isInArena() && event.getBlock().getState() instanceof Sign) {
            if (!plugin.getSignsManager().signs.containsKey(event.getBlock().getLocation())) return;
            plugin.getArenasManager().getArena(plugin.getSignsManager().signs.get(event.getBlock().getLocation())).removeSign(event.getBlock().getLocation());
        }

        if (playerData.isSettingUpArena()) {
            Arena editingArena = plugin.getArenasManager().getArena(playerData.settingUpArena.getName());
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%arena%", playerData.settingUpArena.getName());
            editingArena.checkWorlds(player);
            event.setCancelled(true);

            if (itemInHand.getType() == Material.DIAMOND_AXE) {
                editingArena.setArenaCorner1(event.getBlock().getLocation());
                player.sendMessage(plugin.getMessagesManager().getMessage("arenaSetup.set-corner", placeholders, player));

            } else if (itemInHand.getType() == Material.DIAMOND_HOE) {
                if (editingArena.addArenaAreaSpawn(event.getBlock().getLocation())) {
                    player.sendMessage(plugin.getMessagesManager().getMessage("arenaSetup.add-spawn", placeholders, player));
                } else {
                    player.sendMessage(plugin.getMessagesManager().getMessage("arenaSetup.block-is-spawn", placeholders, player));
                }

            } else if (itemInHand.getType() == Material.GOLDEN_AXE) {
                editingArena.setWaitingCorner1(event.getBlock().getLocation());
                player.sendMessage(plugin.getMessagesManager().getMessage("arenaSetup.set-corner", placeholders, player));

            } else if (itemInHand.getType() == Material.GOLDEN_HOE) {
                if (editingArena.addWaitingAreaSpawn(event.getBlock().getLocation())) {
                    player.sendMessage(plugin.getMessagesManager().getMessage("arenaSetup.add-spawn", placeholders, player));
                } else {
                    player.sendMessage(plugin.getMessagesManager().getMessage("arenaSetup.block-is-spawn", placeholders, player));
                }
            }
        }
    }
}
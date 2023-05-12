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
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = TagGame.playersManager.getPlayerData(player.getName());
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // If the block that was broken was a join sign, remove it from the arena's signs, and the signs list.
        if (!playerData.isSettingUpArena() && !playerData.isInArena() && event.getBlock().getState() instanceof Sign) {
            if (!TagGame.signsManager.signs.containsKey(event.getBlock().getLocation())) return;
            TagGame.arenasManager.getArena(TagGame.signsManager.signs.get(event.getBlock().getLocation())).removeSign(event.getBlock().getLocation());
        }

        if (playerData.isSettingUpArena()) {
            Arena editingArena = TagGame.arenasManager.getArena(playerData.settingUpArena.getName());
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%arena%", playerData.settingUpArena.getName());
            editingArena.checkWorlds(player);
            event.setCancelled(true);

            if (itemInHand.getType() == Material.DIAMOND_AXE) {
                editingArena.setArenaCorner1(event.getBlock().getLocation());
                player.sendMessage(TagGame.messagesManager.getMessage("arenaSetup.set-corner", placeholders, player));

            } else if (itemInHand.getType() == Material.DIAMOND_HOE) {
                if (editingArena.addArenaAreaSpawn(event.getBlock().getLocation())) {
                    player.sendMessage(TagGame.messagesManager.getMessage("arenaSetup.add-spawn", placeholders, player));
                } else {
                    player.sendMessage(TagGame.messagesManager.getMessage("arenaSetup.block-is-spawn", placeholders, player));
                }

            } else if (itemInHand.getType() == Material.GOLDEN_AXE) {
                editingArena.setWaitingCorner1(event.getBlock().getLocation());
                player.sendMessage(TagGame.messagesManager.getMessage("arenaSetup.set-corner", placeholders, player));

            } else if (itemInHand.getType() == Material.GOLDEN_HOE) {
                if (editingArena.addWaitingAreaSpawn(event.getBlock().getLocation())) {
                    player.sendMessage(TagGame.messagesManager.getMessage("arenaSetup.add-spawn", placeholders, player));
                } else {
                    player.sendMessage(TagGame.messagesManager.getMessage("arenaSetup.block-is-spawn", placeholders, player));
                }
            }
        }
    }
}
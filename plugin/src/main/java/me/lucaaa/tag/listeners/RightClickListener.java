package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import me.lucaaa.tag.api.enums.ArenaMode;
import me.lucaaa.tag.api.enums.ArenaTime;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Objects;

public class RightClickListener implements Listener {
    private final TagGame plugin;

    public RightClickListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !playerData.isSettingUpArena() && !playerData.isInArena() && Objects.requireNonNull(event.getClickedBlock()).getState() instanceof Sign) {
            if (!plugin.getSignsManager().isJoinSign(event.getClickedBlock().getLocation())) return;
            plugin.getSignsManager().getArena(event.getClickedBlock().getLocation()).playerJoin(player);
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND || (!playerData.isSettingUpArena() && !playerData.isInArena())) return;

        event.setCancelled(true);
        if (playerData.isSettingUpArena()) {
            Arena editingArena = plugin.getArenasManager().getArena(playerData.settingUpArena.getName());
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%arena%", playerData.settingUpArena.getName());
            editingArena.checkWorlds(player);

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getInventory().getHeldItemSlot() <= 3) {
                switch (itemInHand.getType()) {
                    case DIAMOND_AXE -> {
                        editingArena.setArenaCorner2(Objects.requireNonNull(event.getClickedBlock()).getLocation());
                        plugin.getMessagesManager().sendMessage("arenaSetup.set-corner", placeholders, player);
                    }

                    case DIAMOND_HOE -> {
                        if (editingArena.removeArenaAreaSpawn(Objects.requireNonNull(event.getClickedBlock()).getLocation())) {
                            plugin.getMessagesManager().sendMessage("arenaSetup.remove-spawn", placeholders, player);
                        }
                        else {
                            plugin.getMessagesManager().sendMessage("arenaSetup.block-is-not-spawn", placeholders, player);
                        }
                    }

                    case GOLDEN_AXE -> {
                        editingArena.setWaitingCorner2(Objects.requireNonNull(event.getClickedBlock()).getLocation());
                        plugin.getMessagesManager().sendMessage("arenaSetup.set-corner", placeholders, player);
                    }

                    case GOLDEN_HOE -> {
                        if (editingArena.removeWaitingAreaSpawn(Objects.requireNonNull(event.getClickedBlock()).getLocation())) {
                            plugin.getMessagesManager().sendMessage("arenaSetup.remove-spawn", placeholders, player);
                        }
                        else {
                            plugin.getMessagesManager().sendMessage("arenaSetup.block-is-not-spawn", placeholders, player);
                        }
                    }

                    default -> {}
                }

            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                Long now = System.currentTimeMillis();
                if (now - playerData.interactEventCooldown <= 25) return;

                if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().startsWith("Waiting area:")) {
                    editingArena.setWaitingArenaEnabled(!editingArena.isWaitingAreaEnabled());

                } else if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().startsWith("Minimum players:")) {
                    if (player.isSneaking()) editingArena.setMinPlayers(editingArena.getMinPlayers() - 1);
                    else editingArena.setMinPlayers(editingArena.getMinPlayers() + 1);

                } else if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().startsWith("Maximum players:")) {
                    if (player.isSneaking()) editingArena.setMaxPlayers(editingArena.getMaxPlayers() - 1);
                    else editingArena.setMaxPlayers(editingArena.getMaxPlayers() + 1);

                } else if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().startsWith("Arena time:")) {
                    if (editingArena.getArenaTimeMode() == ArenaTime.LIMITED) editingArena.setArenaTimeMode(ArenaTime.UNLIMITED);
                    else editingArena.setArenaTimeMode(ArenaTime.LIMITED);

                } else if (Objects.requireNonNull(itemInHand.getItemMeta()).getDisplayName().startsWith("Arena mode:")) {
                    if (editingArena.getArenaTimeMode() == ArenaTime.UNLIMITED) {
                        if (editingArena.getArenaMode() == ArenaMode.HIT) editingArena.setArenaMode(ArenaMode.TNT);
                        else editingArena.setArenaMode(ArenaMode.HIT);

                    } else {
                        if (editingArena.getArenaMode() == ArenaMode.HIT) editingArena.setArenaMode(ArenaMode.TIMED_HIT);
                        else if (editingArena.getArenaMode() == ArenaMode.TIMED_HIT) editingArena.setArenaMode(ArenaMode.TNT);
                        else if (editingArena.getArenaMode() == ArenaMode.TNT) editingArena.setArenaMode(ArenaMode.TIMED_TNT);
                        else if (editingArena.getArenaMode() == ArenaMode.TIMED_TNT) editingArena.setArenaMode(ArenaMode.HIT);
                    }
                }

                playerData.updateSetupInventory();
                playerData.interactEventCooldown = now;
            }

        } else {
            if (itemInHand.getType() == plugin.getItemsManager().getItem("leave-item").getType()) {
                playerData.arena.playerLeave(player, true);
                return;
            }

            if (playerData.arena.getTaggers().contains(playerData) && (playerData.arena.getArenaMode() == ArenaMode.TNT || playerData.arena.getArenaMode() == ArenaMode.TIMED_TNT)) {
                Long now = System.currentTimeMillis();
                // Divide / 1000.0 to convert to seconds.
                if ((now - playerData.tntThrowCooldown) / 1000.0 <= plugin.getMainConfig().getConfig().getDouble("tnt-cooldown")) return;

                TNTPrimed tagTNT = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation().add(0.0, 1.0, 0.0), TNTPrimed.class);
                tagTNT.getPersistentDataContainer().set(new NamespacedKey(plugin, "TAG"), PersistentDataType.STRING, event.getPlayer().getName());
                tagTNT.setVelocity(event.getPlayer().getLocation().getDirection().add(new Vector(0.0, 0.15, 0.0)));
                tagTNT.setFuseTicks(plugin.getMainConfig().getConfig().getInt("tnt-fuse-time"));

                playerData.tntThrowCooldown = now;
            }
        }
    }
}
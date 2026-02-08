package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import me.lucaaa.tag.api.enums.ArenaMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class PlayerDamageListener implements Listener {
    private final TagGame plugin;

    public PlayerDamageListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerHunger(FoodLevelChangeEvent event) {
        PlayerData player = plugin.getPlayersManager().getPlayerData((Player) event.getEntity());
        if (player.isInArena()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerData victimData = plugin.getPlayersManager().getPlayerData(victim);
        // If a TNT exploded and hurt someone, cancel the event
        // This line is NOT redundant because that someone might not be in the arena (hence the next even.setCancelled(true) would not be called.
        if (event.getDamager().getPersistentDataContainer().has(plugin.key, PersistentDataType.STRING)) event.setCancelled(true);
        if (!victimData.isInArena()) return;

        event.setCancelled(true);

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
        if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof TNTPrimed)) return;

        Arena arena = victimData.arena;

        if (event.getDamager() instanceof Player attacker && (arena.getArenaMode() == ArenaMode.HIT || arena.getArenaMode() == ArenaMode.TIMED_HIT)) {
            PlayerData attackerData = plugin.getPlayersManager().getPlayerData(attacker);

            ItemStack itemInHand = attacker.getInventory().getItemInMainHand();
            ItemMeta itemInHandMeta = Objects.requireNonNull(itemInHand.getItemMeta());

            if (!attackerData.isInArena() || !itemInHandMeta.getPersistentDataContainer().has(plugin.key, PersistentDataType.STRING)) return;

            // The attacker is a tagger
            if (arena.getTaggers().contains(attackerData)) {
                arena.setTagger(attackerData, victimData);

            } else {
                if (!plugin.getItemsManager().itemExists("push-stick")) return;

                // If neither are taggers, only push if enabled in config.
                if (!arena.getTaggers().contains(victimData) && !plugin.getItemsManager().pushNonTaggers()) return;
            }

        } else if (event.getDamager() instanceof TNTPrimed tagTNT) {
            if (!tagTNT.getPersistentDataContainer().has(plugin.key, PersistentDataType.STRING)) return;
            if (arena.getTaggers().contains(victimData)) return;

            Player player = Bukkit.getPlayer(Objects.requireNonNull(tagTNT.getPersistentDataContainer().get(plugin.key, PersistentDataType.STRING)));
            if (player == null) return;

            if (!arena.getTaggers().contains(plugin.getPlayersManager().getPlayerData(player))) return;

            arena.setTagger(plugin.getPlayersManager().getPlayerData(player), victimData);
        }

        victim.setVelocity(event.getDamager().getLocation().getDirection().multiply(plugin.getMainConfig().getConfig().getDouble("knockback")).setY(plugin.getMainConfig().getConfig().getDouble("height")));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        if (!playerData.isInArena()) return;

        if (plugin.getMainConfig().getConfig().getBoolean("prevent-damage")) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) playerData.getPlayer().setFireTicks(0);
        }
    }
}
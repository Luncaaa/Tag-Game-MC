package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import me.lucaaa.tag.utils.ArenaMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerDamageListener implements Listener {
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerData victimData = TagGame.playersManager.getPlayerData(victim.getName());
        if (event.getDamager().getPersistentDataContainer().has(new NamespacedKey(TagGame.getPlugin(), "TAG"), PersistentDataType.STRING)) event.setCancelled(true);
        if (!victimData.isInArena()) return;

        event.setCancelled(true);

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK && event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
        if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof TNTPrimed)) return;

        Arena arena = victimData.arena;

        if (event.getDamager() instanceof Player attacker && (arena.getArenaMode() == ArenaMode.HIT || arena.getArenaMode() == ArenaMode.TIMED_HIT)) {
            PlayerData attackerData = TagGame.playersManager.getPlayerData(attacker.getName());

            if (!attackerData.isInArena()) return;

            if (!arena.getTaggers().contains(attackerData) || arena.getTaggers().contains(victimData)) return;

            if (attacker.getInventory().getItemInMainHand().getType() == Material.AIR) return;
            ItemMeta itemInHandMeta = attacker.getInventory().getItemInMainHand().getItemMeta();
            assert itemInHandMeta != null;
            if (!itemInHandMeta.getPersistentDataContainer().has(new NamespacedKey(TagGame.getPlugin(), "TAG"), PersistentDataType.STRING)) return;

            arena.setTagger(attackerData, victimData);

        } else if (event.getDamager() instanceof TNTPrimed tagTNT) {
            if (!tagTNT.getPersistentDataContainer().has(new NamespacedKey(TagGame.getPlugin(), "TAG"), PersistentDataType.STRING)) return;
            if (arena.getTaggers().contains(victimData)) return;
            if (!arena.getTaggers().contains(TagGame.playersManager.getPlayerData(tagTNT.getPersistentDataContainer().get(new NamespacedKey(TagGame.getPlugin(), "PLAYER"), PersistentDataType.STRING)))) return;

            arena.setTagger(TagGame.playersManager.getPlayerData(tagTNT.getPersistentDataContainer().get(new NamespacedKey(TagGame.getPlugin(), "PLAYER"), PersistentDataType.STRING)), victimData);
        }

        victim.setVelocity(event.getDamager().getLocation().getDirection().multiply(TagGame.mainConfig.getConfig().getDouble("knockback")).setY(TagGame.mainConfig.getConfig().getDouble("height")));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        PlayerData playerData = TagGame.playersManager.getPlayerData(event.getEntity().getName());
        if (!playerData.isInArena()) return;

        if (TagGame.mainConfig.getConfig().getBoolean("prevent-damage")) {
            event.setCancelled(true);
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) playerData.getPlayer().setFireTicks(0);
        }
    }
}
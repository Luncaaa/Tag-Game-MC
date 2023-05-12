package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityExplodeListener implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;
        if (!event.getEntity().getPersistentDataContainer().has(new NamespacedKey(TagGame.getPlugin(), "TAG"), PersistentDataType.STRING)) return;

        // Prevent blocks from breaking
        event.setCancelled(true);
    }
}
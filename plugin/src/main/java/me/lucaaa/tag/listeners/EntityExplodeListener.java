package me.lucaaa.tag.listeners;

import me.lucaaa.tag.TagGame;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityExplodeListener implements Listener {
    private final TagGame plugin;

    public EntityExplodeListener(TagGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;
        if (!event.getEntity().getPersistentDataContainer().has(plugin.key, PersistentDataType.STRING)) return;

        // Prevent blocks from breaking
        event.setCancelled(true);
    }
}
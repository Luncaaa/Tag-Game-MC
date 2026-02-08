package me.lucaaa.tag.actions.util;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarRunnable {
    private final TagGame plugin;
    private final String message;
    private final int duration;

    public ActionBarRunnable(TagGame plugin, String message, int duration) {
        this.plugin = plugin;
        this.message = message;
        this.duration = duration;
    }

    public void sendToPlayer(Arena arena, Player player) {
        Audience audience = plugin.getAudience(player);
        Component message = plugin.getMessagesManager().getColoredMessage(this.message, arena.getPlaceholders(), player, false);
        new BukkitRunnable() {
            private  int timeLeft = duration;
            @Override
            public void run() {
                audience.sendActionBar(message);
                --timeLeft;
                if (timeLeft == 0) this.cancel();
            }
        }.runTaskTimer(plugin, 0L, 0L);
    }
}
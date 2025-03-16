package me.lucaaa.tag.actions.util;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.actionTypes.ActionbarAction;
import me.lucaaa.tag.game.Arena;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionBarRunnable {
    private final TagGame plugin;
    private final ActionbarAction action;
    private final String message;
    private final int duration;

    public ActionBarRunnable(TagGame plugin, ActionbarAction action, String message, int duration) {
        this.plugin = plugin;
        this.action = action;
        this.message = message;
        this.duration = duration;
    }

    public void sendToPlayer(Arena arena, Player player) {
        BaseComponent[] message = action.getTextComponent(this.message, player, arena.getPlaceholders());
        new BukkitRunnable() {
            private  int timeLeft = duration;
            @Override
            public void run() {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
                --timeLeft;
                if (timeLeft == 0) this.cancel();
            }
        }.runTaskTimer(plugin, 0L, 0L);
    }
}
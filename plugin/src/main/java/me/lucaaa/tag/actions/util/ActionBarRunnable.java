package me.lucaaa.tag.actions.util;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.actionTypes.ActionbarAction;
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

    public void sendToPlayer(Player clickedPlayer, Player actionPlayer) {
        BaseComponent[] component = this.action.getTextComponent(this.message, clickedPlayer, actionPlayer);
        new BukkitRunnable() {
            private  int timeLeft = duration;
            @Override
            public void run() {
                actionPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                --timeLeft;
                if (timeLeft == 0) this.cancel();
            }
        }.runTaskTimer(plugin, 0L, 0L);
    }
}
package me.lucaaa.tag.actions.actionTypes;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.Action;
import me.lucaaa.tag.actions.util.ActionBarRunnable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionbarAction extends Action {
    private final ActionBarRunnable runnable;

    public ActionbarAction(TagGame plugin, ConfigurationSection actionSection) {
        super(List.of("message", "duration"), actionSection);
        String message = actionSection.getString("message");
        int duration = actionSection.getInt("duration");
        this.runnable = new ActionBarRunnable(plugin, this, message, duration);
    }

    @Override
    public void runAction(Player clickedPlayer, Player actionPlayer) {
        this.runnable.sendToPlayer(clickedPlayer, actionPlayer);
    }
}
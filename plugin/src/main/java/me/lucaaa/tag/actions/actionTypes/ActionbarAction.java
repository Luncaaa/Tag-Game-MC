package me.lucaaa.tag.actions.actionTypes;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.Action;
import me.lucaaa.tag.actions.util.ActionBarRunnable;
import me.lucaaa.tag.game.Arena;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionbarAction extends Action {
    private final ActionBarRunnable runnable;

    public ActionbarAction(TagGame plugin, ConfigurationSection actionSection) {
        super(plugin, List.of("message", "duration"), actionSection);
        String message = actionSection.getString("message");
        int duration = actionSection.getInt("duration");
        this.runnable = new ActionBarRunnable(plugin, message, duration);
    }

    @Override
    public void runAction(Arena arena, Player player) {
        this.runnable.sendToPlayer(arena, player);
    }
}
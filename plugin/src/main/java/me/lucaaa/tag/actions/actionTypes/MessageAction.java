package me.lucaaa.tag.actions.actionTypes;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.Action;
import me.lucaaa.tag.game.Arena;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageAction extends Action {
    private final String message;

    public MessageAction(TagGame plugin, ConfigurationSection actionSection) {
        super(plugin, List.of("message"), actionSection);
        this.message = actionSection.getString("message");
    }

    @Override
    public void runAction(Arena arena, Player player) {
        player.sendMessage(getText(message, player, arena.getPlaceholders()));
    }
}
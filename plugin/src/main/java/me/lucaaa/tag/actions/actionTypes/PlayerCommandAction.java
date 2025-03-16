package me.lucaaa.tag.actions.actionTypes;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.Action;
import me.lucaaa.tag.game.Arena;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PlayerCommandAction extends Action {
    private final String command;
    private final String arguments;

    public PlayerCommandAction(TagGame plugin, ConfigurationSection actionSection) {
        super(plugin, List.of("command"), actionSection);

        List<String> fullCommand = new LinkedList<>(Arrays.asList(actionSection.getString("command", "").split(" ")));
        this.command = fullCommand.remove(0);
        this.arguments = String.join(" ", fullCommand);
    }

    @Override
    public void runAction(Arena arena, Player player) {
        player.performCommand(this.command + " " + getTextString(this.arguments, player, arena.getPlaceholders()));
    }
}
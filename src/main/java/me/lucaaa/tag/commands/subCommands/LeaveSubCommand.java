package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class LeaveSubCommand extends SubCommandsFormat {
    public LeaveSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "leave";
        this.description = "Leave an arena.";
        this.usage = "/tag leave";
        this.minArguments = 0;
        this.executableByConsole = false;
        this.neededPermission = null;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData((Player) sender);

        if (playerData.arena == null) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.not-in-arena", null, sender));
            return;
        }

        playerData.arena.playerLeave((Player) sender, true);
    }
}
package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetLobbySubCommand extends SubCommandsFormat {
    public SetLobbySubCommand() {
        this.name = "setLobby";
        this.description = "Sets the main lobby.";
        this.usage = "/tag setLobby";
        this.minArguments = 0;
        this.executableByConsole = false;
        this.neededPermission = "tag.setlobby";
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        Location playerLocation = ((Player) sender).getLocation();
        TagGame.mainConfig.getConfig().set("lobby", playerLocation);
        TagGame.mainConfig.save();
        sender.sendMessage(TagGame.messagesManager.getMessage("commands.lobby-set", null, sender));
    }
}

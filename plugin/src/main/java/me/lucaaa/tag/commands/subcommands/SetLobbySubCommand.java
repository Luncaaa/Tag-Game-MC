package me.lucaaa.tag.commands.subcommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class SetLobbySubCommand extends SubCommandsFormat {
    public SetLobbySubCommand(TagGame plugin) {
        super(plugin);
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
        plugin.getMainConfig().getConfig().set("lobby", playerLocation);
        plugin.getMainConfig().save();
        plugin.getMessagesManager().sendMessage("commands.lobby-set", null, sender);
    }
}

package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.sql.SQLException;

public class ReloadSubCommand extends SubCommandsFormat {
    public ReloadSubCommand() {
        this.name = "reload";
        this.description = "Reloads the plugin's configuration files.";
        this.usage = "/tag reload";
        this.minArguments = 0;
        this.executableByConsole = true;
        this.neededPermission = "tag.reload";
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        try {
            TagGame.reloadConfigs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sender.sendMessage(TagGame.messagesManager.getMessage("commands.reload-successful", null, sender));
    }
}
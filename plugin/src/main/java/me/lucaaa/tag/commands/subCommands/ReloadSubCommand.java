package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class ReloadSubCommand extends SubCommandsFormat {
    public ReloadSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "reload";
        this.description = "Reloads the plugin's configuration files.";
        this.usage = "/tag reload";
        this.minArguments = 0;
        this.executableByConsole = true;
        this.neededPermission = "tag.reload";
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        plugin.reloadConfigs();
        sender.sendMessage(plugin.getMessagesManager().getMessage("commands.reload-successful", null, sender));
    }
}
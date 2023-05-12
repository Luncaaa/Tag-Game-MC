package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class HelpSubCommand extends SubCommandsFormat {
    public HelpSubCommand() {
        this.name = "help";
        this.description = "Information about the commands the plugin has.";
        this.usage = "/tag help";
        this.minArguments = 0;
        this.executableByConsole = true;
        this.neededPermission = null;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        for (SubCommandsFormat value : TagGame.subCommands.values()) {
            if (value.neededPermission == null || sender.hasPermission(value.neededPermission) || sender.hasPermission("tag.admin")) {
                sender.sendMessage(value.usage + " - " + value.description);
            }
        }
    }
}

package me.lucaaa.tag.commands.subcommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Map;

public class HelpSubCommand extends SubCommandsFormat {
    private final Map<String, SubCommandsFormat> subCommands;

    public HelpSubCommand(TagGame plugin, Map<String, SubCommandsFormat> subCommands) {
        super(plugin);
        this.name = "help";
        this.description = "Information about the commands the plugin has.";
        this.usage = "/tag help";
        this.minArguments = 0;
        this.executableByConsole = true;
        this.neededPermission = null;
        this.subCommands = subCommands;
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        sender.sendMessage(plugin.getMessagesManager().getColoredMessage("&c---------[ Tag Game help menu ]---------", false));

        sender.sendMessage(plugin.getMessagesManager().getColoredMessage("&cCommands: &7&o([] - mandatory args, <> - optional args)", false));
        for (SubCommandsFormat value : this.subCommands.values()) {
            if (value.neededPermission == null || sender.hasPermission(value.neededPermission) || sender.hasPermission("tag.admin")) {
                sender.sendMessage(plugin.getMessagesManager().getColoredMessage(" &7- &6" + value.usage + "&7: &e" + value.description, false));
            }
        }
    }
}
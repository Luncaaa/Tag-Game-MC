package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.HashMap;

public class CreateArenaSubCommand extends SubCommandsFormat {
    public CreateArenaSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "createArena";
        this.description = "Creates an arena.";
        this.usage = "/tag createArena [name]";
        this.minArguments = 1;
        this.executableByConsole = true;
        this.neededPermission = "tag.createarena";
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        boolean couldCreateArena = plugin.getArenasManager().createArena(args[1]);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (couldCreateArena) {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.creation-success", placeholders, sender));
        } else {
            sender.sendMessage(plugin.getMessagesManager().getMessage("commands.creation-failure", placeholders, sender));
        }
    }
}

package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RemoveArenaSubCommand extends SubCommandsFormat {
    public RemoveArenaSubCommand() {
        this.name = "removeArena";
        this.description = "Removes an arena.";
        this.usage = "/tag removeArena [name]";
        this.minArguments = 1;
        this.executableByConsole = true;
        this.neededPermission = "tag.removearena";
    }

    @Override
    public ArrayList<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>(TagGame.arenasManager.arenas.keySet().stream().toList());
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        boolean couldRemoveArena = TagGame.arenasManager.deleteArena(args[1]);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (couldRemoveArena) {
            sender.sendMessage(TagGame.messagesManager.getMessage("commands.removal-success", placeholders, sender));
        } else {
            sender.sendMessage(TagGame.messagesManager.getMessage("commands.removal-failure", placeholders, sender));
        }
    }
}

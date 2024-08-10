package me.lucaaa.tag.commands.subcommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveArenaSubCommand extends SubCommandsFormat {
    public RemoveArenaSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "removeArena";
        this.description = "Removes an arena.";
        this.usage = "/tag removeArena [name]";
        this.minArguments = 1;
        this.executableByConsole = true;
        this.neededPermission = "tag.removearena";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return plugin.getArenasManager().arenas.keySet().stream().toList();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        boolean couldRemoveArena = plugin.getArenasManager().deleteArena(args[1]);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (couldRemoveArena) {
            plugin.getMessagesManager().sendMessage("commands.removal-success", placeholders, sender);
        } else {
            plugin.getMessagesManager().sendMessage("commands.removal-failure", placeholders, sender);
        }
    }
}

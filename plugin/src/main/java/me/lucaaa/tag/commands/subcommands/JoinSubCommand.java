package me.lucaaa.tag.commands.subcommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinSubCommand extends SubCommandsFormat {
    public JoinSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "join";
        this.description = "Join an arena.";
        this.usage = "/tag join [arena]";
        this.minArguments = 1;
        this.executableByConsole = false;
        this.neededPermission = "tag.join";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return plugin.getArenasManager().arenas.keySet().stream().toList();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        if (plugin.getPlayersManager().getPlayerData((Player) sender).arena != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%arena%", plugin.getPlayersManager().getPlayerData((Player) sender).arena.getName());
            plugin.getMessagesManager().sendMessage("commands.already-in-arena", placeholders, sender);
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (plugin.getArenasManager().arenas.get(args[1]) == null) {
            plugin.getMessagesManager().sendMessage("commands.arena-not-found", placeholders, sender);
            return;
        }

        plugin.getArenasManager().arenas.get(args[1]).playerJoin((Player) sender);
    }
}
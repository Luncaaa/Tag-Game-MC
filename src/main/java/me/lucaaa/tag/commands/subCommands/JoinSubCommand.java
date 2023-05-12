package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JoinSubCommand extends SubCommandsFormat {
    public JoinSubCommand() {
        this.name = "join";
        this.description = "Join an arena.";
        this.usage = "/tag join [arena]";
        this.minArguments = 1;
        this.executableByConsole = false;
        this.neededPermission = "tag.join";
    }

    @Override
    public ArrayList<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>(TagGame.arenasManager.arenas.keySet().stream().toList());
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        if (TagGame.playersManager.getPlayerData(sender.getName()).arena != null) {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%arena%", TagGame.playersManager.getPlayerData(sender.getName()).arena.getName());
            sender.sendMessage(TagGame.messagesManager.getMessage("commands.already-in-arena", placeholders, sender));
            return;
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (TagGame.arenasManager.arenas.get(args[1]) == null) {
            sender.sendMessage(TagGame.messagesManager.getMessage("commands.arena-not-found", placeholders, sender));
            return;
        }

        TagGame.arenasManager.arenas.get(args[1]).playerJoin((Player) sender);
    }
}
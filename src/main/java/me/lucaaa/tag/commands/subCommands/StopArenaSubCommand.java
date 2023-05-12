package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.utils.StopCause;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StopArenaSubCommand extends SubCommandsFormat {
    public StopArenaSubCommand() {
        this.name = "stop";
        this.description = "Stops an arena that is running.";
        this.usage = "/tag stop [arena]";
        this.minArguments = 1;
        this.executableByConsole = true;
        this.neededPermission = "tag.stop";
    }

    @Override
    public ArrayList<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>(TagGame.arenasManager.arenas.keySet().stream().toList());
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        Arena arena = TagGame.arenasManager.getArena(args[1]);
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (!arena.isRunning()) {
            sender.sendMessage(TagGame.messagesManager.getMessage("commands.arena-not-running", placeholders, sender));
            return;
        }

        arena.stopGame(StopCause.COMMAND, false);
        sender.sendMessage(TagGame.messagesManager.getMessage("commands.stop-success", placeholders, sender));
    }
}
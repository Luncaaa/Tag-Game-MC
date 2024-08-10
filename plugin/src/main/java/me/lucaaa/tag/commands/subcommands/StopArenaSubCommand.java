package me.lucaaa.tag.commands.subcommands;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.api.enums.StopCause;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopArenaSubCommand extends SubCommandsFormat {
    public StopArenaSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "stop";
        this.description = "Stops an arena that is running.";
        this.usage = "/tag stop [arena]";
        this.minArguments = 1;
        this.executableByConsole = true;
        this.neededPermission = "tag.stop";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return plugin.getArenasManager().arenas.keySet().stream().toList();
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        Arena arena = plugin.getArenasManager().getArena(args[1]);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        if (!arena.isRunning()) {
            plugin.getMessagesManager().sendMessage("commands.arena-not-running", placeholders, sender);
            return;
        }

        arena.stopGame(StopCause.COMMAND, false);
        plugin.getMessagesManager().sendMessage("commands.stop-success", placeholders, sender);
    }
}
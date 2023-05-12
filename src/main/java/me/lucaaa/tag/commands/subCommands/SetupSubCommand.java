package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class SetupSubCommand extends SubCommandsFormat {
    public SetupSubCommand() {
        this.name = "setup";
        this.description = "Modify an arena and its settings.";
        this.usage = "/tag setup [arena] <setWorld / setFinishTime / setTaggersNumber> <value>";
        this.minArguments = 1;
        this.executableByConsole = false;
        this.neededPermission = "tag.setup";
    }

    @Override
    public ArrayList<String> getTabCompletions(CommandSender sender, String[] args) {
        switch (args.length) {
            case 2 -> {
                return new ArrayList<>(TagGame.arenasManager.arenas.keySet().stream().toList());
            }
            case 3 -> {
                return new ArrayList<>(Arrays.asList("setFinishTime", "setWorld", "setTaggersNumber"));
            }
            case 4 -> {
                // Gets the names of all the existing worlds.
                if (Objects.equals(args[2], "setWorld")) return new ArrayList<>(Bukkit.getWorlds().stream().map(World::getName).toList());
                else return new ArrayList<>();
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        Player player = (Player) sender;
        PlayerData playerData = TagGame.playersManager.getPlayerData(player.getName());
        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", args[1]);

        switch (args.length) {
            case 2 -> {
                if (TagGame.arenasManager.arenas.get(args[1]) == null) {
                    player.sendMessage(TagGame.messagesManager.getMessage("commands.arena-not-found", placeholders, player));
                    return;
                }

                playerData.settingUpArena = TagGame.arenasManager.getArena(args[1]);
                playerData.giveSetupInventory();
                player.sendMessage(TagGame.messagesManager.getMessage("commands.started-setup", placeholders, player));
            }

            case 3 -> player.sendMessage(TagGame.messagesManager.getMessage("commands.missing-argument", placeholders, player));

            default -> {
                if (Objects.equals(args[2], "setFinishTime")) {
                    try {
                        placeholders.put("%time%", args[3]);
                        TagGame.arenasManager.getArena(args[1]).setTimeEnd(Integer.parseInt(args[3]));
                        player.sendMessage(TagGame.messagesManager.getMessage("commands.changed-time", placeholders, player));
                    } catch (NumberFormatException exception) {
                        placeholders.put("%number%", args[3]);
                        player.sendMessage(TagGame.messagesManager.getMessage("commands.invalid-number", placeholders, player));
                    }

                } else if (Objects.equals(args[2], "setWorld")) {
                    placeholders.put("%world%", args[3]);
                    TagGame.arenasManager.getArena(args[1]).setWorld(args[3]);
                    player.sendMessage(TagGame.messagesManager.getMessage("commands.set-world", placeholders, player));

                } else if (Objects.equals(args[2], "setTaggersNumber")) {
                    placeholders.put("%number%", args[3]);
                    try {
                        TagGame.arenasManager.getArena(args[1]).setTaggersNumber(Integer.parseInt(args[3]));
                        player.sendMessage(TagGame.messagesManager.getMessage("commands.changed-taggers-number", placeholders, player));
                    } catch (NumberFormatException exception) {
                        player.sendMessage(TagGame.messagesManager.getMessage("commands.invalid-number", placeholders, player));
                    }
                }
            }
        }
    }
}

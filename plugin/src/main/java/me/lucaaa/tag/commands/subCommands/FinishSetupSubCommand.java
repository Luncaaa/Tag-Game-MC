package me.lucaaa.tag.commands.subCommands;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;

public class FinishSetupSubCommand extends SubCommandsFormat {
    public FinishSetupSubCommand(TagGame plugin) {
        super(plugin);
        this.name = "finishSetup";
        this.description = "Stop modifying an arena and its settings.";
        this.usage = "/tag finishSetup";
        this.minArguments = 0;
        this.executableByConsole = false;
        this.neededPermission = "tag.setup";
    }

    @Override
    public void run(CommandSender sender, String[] args) throws IOException {
        Player player = (Player) sender;
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);

        if (!playerData.isSettingUpArena()) {
            plugin.getMessagesManager().sendMessage("commands.not-setting-up", null, player);

        } else {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("%arena%", playerData.settingUpArena.getName());
            playerData.settingUpArena = null;
            playerData.restoreSavedData();
            plugin.getMessagesManager().sendMessage("commands.finish-setup", placeholders, player);
        }
    }
}
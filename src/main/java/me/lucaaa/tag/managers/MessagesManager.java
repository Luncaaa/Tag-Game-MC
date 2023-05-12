package me.lucaaa.tag.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessagesManager {
    private final HashMap<String, String> messages = new HashMap<>();
    private final HashMap<String, ArrayList<String>> messagesList = new HashMap<>();

    public MessagesManager(YamlConfiguration langConfig) {
        // Each key that is not a config section is added to the map along with its corresponding message
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isConfigurationSection(key)) continue;
            if (langConfig.isString(key)) this.messages.put(key, langConfig.getString(key));
            if (langConfig.isList(key)) {
                this.messagesList.put(key, new ArrayList<>(langConfig.getStringList(key)));
            }
        }
    }

    // Gets a message from the language config the user has set in config.yml
    public String getMessage(String key, HashMap<String, String> placeholders, CommandSender sender) {
        return this.getMessage(key, placeholders, sender, true);
    }

    public String getMessage(String key, HashMap<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (!this.messages.containsKey(key)) {
            Logger.log(Level.SEVERE, "The key \"" + key + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        String message = this.messages.get(key);
        if (addPrefix) message = TagGame.mainConfig.getConfig().getString("prefix") + " " + message;

        if (placeholders != null) message = this.replacePlaceholders(message, placeholders);
        if (sender instanceof Player && TagGame.isPAPIInstalled()) message = PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, message);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessageFromList(String key, int index, HashMap<String, String> placeholders, CommandSender sender) {
        if (!this.messagesList.containsKey(key) || index > this.messagesList.get(key).size() - 1) {
            Logger.log(Level.SEVERE, "The key \"" + key + "." + index + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        String message = this.messagesList.get(key).get(index);

        if (placeholders != null) message = this.replacePlaceholders(message, placeholders);
        if (sender instanceof Player && TagGame.isPAPIInstalled()) message = PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, message);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public ArrayList<String> getMessagesList(String key) {
        if (!this.messagesList.containsKey(key)) {
            ArrayList<String> notFound = new ArrayList<>();
            notFound.add("Messages not found.");
            return notFound;
        }
        return this.messagesList.get(key);
    }

    // Loops through the placeholder map and replaces the keys with the values in the provided string.
    private String replacePlaceholders(String message, HashMap<String, String> placeholders) {
        String newMessage = message;
        placeholders.put("%prefix%", TagGame.mainConfig.getConfig().getString("prefix"));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            newMessage = newMessage.replace(entry.getKey(), entry.getValue());
        }
        return newMessage;
    }
}
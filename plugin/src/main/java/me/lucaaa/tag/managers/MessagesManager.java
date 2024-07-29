package me.lucaaa.tag.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lucaaa.tag.utils.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class MessagesManager {
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, ArrayList<String>> messagesList = new HashMap<>();
    private final String prefix;
    private final boolean isPapiInstalled;

    public MessagesManager(YamlConfiguration langConfig, String prefix, boolean isPapiInstalled) {
        this.prefix = prefix;
        this.isPapiInstalled = isPapiInstalled;

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
    public String getMessage(String key, Map<String, String> placeholders, CommandSender sender) {
        return this.getMessage(key, placeholders, sender, true, true);
    }

    public String getMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix, boolean replaceColors) {
        if (!this.messages.containsKey(key)) {
            Logger.log(Level.WARNING, "The key \"" + key + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        String message = this.messages.get(key);
        if (addPrefix) message = prefix + " " + message;

        if (placeholders != null) message = this.replacePlaceholders(message, placeholders);
        if (sender instanceof Player && isPapiInstalled) message = PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, message);

        return (replaceColors) ? this.componentToString((TextComponent) MiniMessage.miniMessage().deserialize(message), null) : message;
    }

    public String getMessageFromList(String key, int index, Map<String, String> placeholders, CommandSender sender) {
        if (!this.messagesList.containsKey(key) || index > this.messagesList.get(key).size() - 1) {
            Logger.log(Level.WARNING, "The key \"" + key + "." + index + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        String message = this.messagesList.get(key).get(index);

        if (placeholders != null) message = this.replacePlaceholders(message, placeholders);
        if (sender instanceof Player && isPapiInstalled) message = PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, message);

        return this.componentToString((TextComponent) MiniMessage.miniMessage().deserialize(message), null);
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
    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        String newMessage = message;
        placeholders.put("%prefix%", prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            newMessage = newMessage.replace(entry.getKey(), entry.getValue());
        }
        return newMessage;
    }

    public String getColoredMessage(String message, boolean addPrefix) {
        String messageToSend = message;
        if (addPrefix) messageToSend =  prefix + " " + messageToSend;

        return this.componentToString((TextComponent) MiniMessage.miniMessage().deserialize(messageToSend), null);
    }

    private String componentToString(TextComponent component, TextColor parentColor) {
        StringBuilder componentString = new StringBuilder(component.content());

        if (component.hasDecoration(TextDecoration.BOLD)) componentString.insert(0, ChatColor.BOLD);
        if (component.hasDecoration(TextDecoration.UNDERLINED)) componentString.insert(0, ChatColor.UNDERLINE);
        if (component.hasDecoration(TextDecoration.STRIKETHROUGH)) componentString.insert(0, ChatColor.STRIKETHROUGH);
        if (component.hasDecoration(TextDecoration.OBFUSCATED)) componentString.insert(0, ChatColor.MAGIC);

        ChatColor color;
        if (component.color() == null && parentColor == null) {
            color = ChatColor.WHITE;
        } else if (component.color() != null) {
            color = ChatColor.of(Objects.requireNonNull(component.color()).asHexString());
        } else {
            color = ChatColor.of(parentColor.asHexString());
        }

        componentString.insert(0, color).insert(0, "");

        if (component.children().isEmpty()) return ChatColor.translateAlternateColorCodes('&', componentString.toString());

        for (Component child : component.children()) {
            componentString.append(componentToString((TextComponent) child, component.color()));
        }

        return ChatColor.translateAlternateColorCodes('&', componentString.toString());
    }
}
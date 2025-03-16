package me.lucaaa.tag.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lucaaa.tag.TagGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MessagesManager {
    private final TagGame plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacyString = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private final BungeeComponentSerializer bungeeComponent = BungeeComponentSerializer.get();
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> messagesList = new HashMap<>();
    private final String prefix;

    public MessagesManager(TagGame plugin, YamlConfiguration langConfig, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;

        // Each key that is not a config section is added to the map along with its corresponding message
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isConfigurationSection(key)) continue;
            if (langConfig.isString(key)) this.messages.put(key, langConfig.getString(key));
            if (langConfig.isList(key)) {
                this.messagesList.put(key, langConfig.getStringList(key));
            }
        }
    }

    // Gets a message from the language config the user has set in config.yml
    public void sendMessage(String key, Map<String, String> placeholders, CommandSender sender) {
        sendMessage(key, placeholders, sender, true);
    }

    public void sendMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        sender.spigot().sendMessage(BungeeComponentSerializer.get().serialize(getMessage(key, placeholders, sender, addPrefix)));
    }

    /**
     * Gets a message from the config file without parsing the colors. Useful if it'll be combined with another unparsed message.
     * Combining parsed and unparsed messages could result in an error when trying to parse it later.
     * @param key The key from the language file.
     * @param placeholders The placeholders to replace.
     * @param sender To whom this message will be sent.
     * @param addPrefix Whether the prefix should be added or not.
     * @return The message without parsed colors.
     */
    public String getUncoloredMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (!messages.containsKey(key)) {
            plugin.log(Level.WARNING, "The key \"" + key + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        return parseMessage(messages.get(key), placeholders, sender, addPrefix);
    }

    public String getParsedMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        return legacyString.serialize(getMessage(key, placeholders, sender, addPrefix));
    }

    public String getMessageFromList(String key, int index, Map<String, String> placeholders, CommandSender sender) {
        if (!messagesList.containsKey(key) || index > messagesList.get(key).size() - 1) {
            plugin.log(Level.WARNING, "The key \"" + key + "." + index + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        return legacyString.serialize(getColoredMessage(messagesList.get(key).get(index), placeholders, sender, false));
    }

    public List<String> getMessagesList(String key) {
        if (!messagesList.containsKey(key)) {
            List<String> notFound = new ArrayList<>();
            notFound.add("Messages not found.");
            return notFound;
        }
        return messagesList.get(key);
    }

    // Loops through the placeholder map and replaces the keys with the values in the provided string.
    public String replacePlaceholders(String message, Map<String, String> placeholders) {
        String newMessage = message;
        placeholders.put("%prefix%", prefix);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            newMessage = newMessage.replace(entry.getKey(), entry.getValue());
        }
        return newMessage;
    }

    public Component getMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (!messages.containsKey(key)) {
            plugin.log(Level.WARNING, "The key \"" + key + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return miniMessage.deserialize("Message not found.");
        }

        return getColoredMessage(messages.get(key), placeholders, sender, addPrefix);
    }

    public String getColoredMessage(String message, boolean addPrefix) {
        return legacyString.serialize(getColoredMessage(message, null, null, addPrefix));
    }

    public Component getColoredMessage(String message, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        message = parseMessage(message, placeholders, sender, addPrefix);

        // From legacy and minimessage format to a component
        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        // From component to Minimessage String. Replacing the "\" with nothing makes the minimessage formats work.
        String minimessage = miniMessage.serialize(legacy).replace("\\", "");
        // From Minimessage String to Minimessage component
        return miniMessage.deserialize(minimessage);
        // From Minimessage component to legacy string.
        // return legacyString.serialize(component);
    }

    public String toLegacy(Component component) {
        return legacyString.serialize(component);
    }

    public BaseComponent[] toBungee(Component component) {
        return bungeeComponent.serialize(component);
    }

    private String parseMessage(String message, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (addPrefix) message = prefix + "&r " + message;

        if (placeholders != null) message = replacePlaceholders(message, placeholders);
        if (sender instanceof Player && plugin.isPAPIInstalled()) message = PlaceholderAPI.setPlaceholders((Player) sender, message);

        return message;
    }
}
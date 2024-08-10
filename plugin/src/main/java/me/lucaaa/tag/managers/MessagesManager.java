package me.lucaaa.tag.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lucaaa.tag.utils.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MessagesManager {
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> messagesList = new HashMap<>();
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
    public void sendMessage(String key, Map<String, String> placeholders, CommandSender sender) {
        sendMessage(key, placeholders, sender, true);
    }

    public void sendMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        sender.spigot().sendMessage(getMessage(key, placeholders, sender, addPrefix));
    }
    
    public String getUncoloredMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (!messages.containsKey(key)) {
            Logger.log(Level.WARNING, "The key \"" + key + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        return parseMessage(messages.get(key), placeholders, sender, addPrefix);
    }

    public String getParsedMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        return BaseComponent.toLegacyText(getMessage(key, placeholders, sender, addPrefix));
    }

    public String getMessageFromList(String key, int index, Map<String, String> placeholders, CommandSender sender) {
        if (!messagesList.containsKey(key) || index > messagesList.get(key).size() - 1) {
            Logger.log(Level.WARNING, "The key \"" + key + "." + index + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return "Message not found.";
        }

        String message = parseMessage(messagesList.get(key).get(index), placeholders, sender, false);
        return BaseComponent.toLegacyText(parseMessage(message));
    }

    public List<String> getMessagesList(String key) {
        if (!messagesList.containsKey(key)) {
            ArrayList<String> notFound = new ArrayList<>();
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

    public BaseComponent[] getMessage(String key, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (!messages.containsKey(key)) {
            Logger.log(Level.WARNING, "The key \"" + key + "\" was not found in your language file. Try to delete the file and generate it again to solve this issue.");
            return new TextComponent[]{new TextComponent("Message not found.")};
        }

        String message = parseMessage(messages.get(key), placeholders, sender, addPrefix);
        return parseMessage(message);
    }

    public String getColoredMessage(String message, boolean addPrefix) {
        return getColoredMessage(message, null, null, addPrefix);
    }

    public String getColoredMessage(String message, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        String msg = parseMessage(message, placeholders, sender, addPrefix);
        return BaseComponent.toLegacyText(parseMessage(msg));
    }

    private BaseComponent[] parseMessage(String message) {
        // From legacy and minimessage format to a component
        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        // From component to Minimessage String. Replacing the "\" with nothing makes the minimessage formats work.
        String minimessage = MiniMessage.miniMessage().serialize(legacy).replace("\\", "");
        // From Minimessage String to Minimessage component
        Component component = MiniMessage.miniMessage().deserialize(minimessage);
        // From Minimessage component to legacy string.
        return BungeeComponentSerializer.get().serialize(component);
    }

    private String parseMessage(String message, Map<String, String> placeholders, CommandSender sender, boolean addPrefix) {
        if (addPrefix) message = prefix + "&r " + message;

        if (placeholders != null) message = replacePlaceholders(message, placeholders);
        if (sender instanceof Player && isPapiInstalled) message = PlaceholderAPI.setPlaceholders((Player) sender, message);
        return message;
    }

    /* Old method - only parses color (no click actions)
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
    }*/
}
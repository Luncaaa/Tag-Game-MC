package me.lucaaa.tag.actions;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Action {
    protected final TagGame plugin;
    private final int delay;
    private boolean correctFormat = true;
    private final List<String> missingFields = new ArrayList<>();
    protected boolean isCorrect = true;

    public Action(TagGame plugin, List<String> requiredFields, ConfigurationSection section) {
        this.plugin = plugin;
        this.delay = section.getInt("delay", 0);

        for (String requiredField : requiredFields) {
            if (section.get(requiredField) == null) {
                missingFields.add(requiredField);
                this.correctFormat = false;
            }
        }
    }

    /**
     * Runs the action for a specific player.
     * @param player Who to run the action for.
     */
    public abstract void runAction(Arena arena, Player player);

    public int getDelay() {
        return this.delay;
    }

    public boolean isFormatCorrect() {
        return this.correctFormat;
    }

    public List<String> getMissingFields() {
        return this.missingFields;
    }

    public boolean isCorrect() {
        return this.isCorrect;
    }

    public BaseComponent[] getTextComponent(String message, Player player, Map<String, String> placeholders) {
        message = message.replace("%player%", player.getName());
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        if (placeholders != null) {
            message = plugin.getMessagesManager().replacePlaceholders(message, placeholders);
        }

        // From legacy and minimessage format to a component
        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        // From component to Minimessage String. Replacing the "\" with nothing makes the minimessage formats work.
        String minimessage = MiniMessage.miniMessage().serialize(legacy).replace("\\", "");
        // From Minimessage String to Minimessage component
        Component component = MiniMessage.miniMessage().deserialize(minimessage);
        // From Minimessage component to legacy string.
        return BungeeComponentSerializer.get().serialize(component);
    }

    public String getTextString(String message, Player player, Map<String, String> placeholders) {
        return BaseComponent.toLegacyText(getTextComponent(message, player, placeholders));
    }
}
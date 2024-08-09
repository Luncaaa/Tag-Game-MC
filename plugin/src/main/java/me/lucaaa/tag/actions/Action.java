package me.lucaaa.tag.actions;

import me.clip.placeholderapi.PlaceholderAPI;
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

public abstract class Action {
    private final int delay;
    private final boolean global;
    private final boolean globalPlaceholders;
    private boolean correctFormat = true;
    private final List<String> missingFields = new ArrayList<>();
    protected boolean isCorrect = true;

    public Action(List<String> requiredFields, ConfigurationSection section, boolean canBeGlobal) {
        this.delay = section.getInt("delay", 0);
        this.global = canBeGlobal && section.getBoolean("global", false);
        this.globalPlaceholders = section.getBoolean("global-placeholders", true);

        for (String requiredField : requiredFields) {
            if (section.get(requiredField) == null) {
                missingFields.add(requiredField);
                this.correctFormat = false;
            }
        }
    }

    public Action(List<String> requiredFields, ConfigurationSection section) {
        this(requiredFields, section, true);
    }

    /**
     * Runs the action for a specific player.
     * @param clickedPlayer The player who clicked the display.
     * @param actionPlayer Who to run the action for.
     */
    public abstract void runAction(Player clickedPlayer, Player actionPlayer);

    public int getDelay() {
        return this.delay;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public boolean useGlobalPlaceholders() {
        return this.globalPlaceholders;
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

    public BaseComponent[] getTextComponent(String message, Player clickedPlayer, Player globalPlayer) {
        message = message.replace("%player%", clickedPlayer.getName());
        if (globalPlayer != null) message = message.replace("%global_player%", globalPlayer.getName());

        Player placeholderPlayer = (useGlobalPlaceholders()) ? globalPlayer : clickedPlayer;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(placeholderPlayer, message);
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

    public String getTextString(String message, Player clickedPlayer, Player actionPlayer) {
        return BaseComponent.toLegacyText(getTextComponent(message, clickedPlayer, actionPlayer));
    }
}
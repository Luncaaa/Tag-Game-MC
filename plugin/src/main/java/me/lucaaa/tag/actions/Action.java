package me.lucaaa.tag.actions;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.managers.MessagesManager;
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

    public String getTextString(String message, Player player, Map<String, String> placeholders) {
        message = message.replace("%player%", player.getName());
        MessagesManager manager = plugin.getMessagesManager();
        return manager.toLegacy(manager.getColoredMessage(message, placeholders, player, false));
    }
}
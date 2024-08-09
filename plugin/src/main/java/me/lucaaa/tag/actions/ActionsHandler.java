package me.lucaaa.tag.actions;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.actionTypes.*;
import me.lucaaa.tag.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

public class ActionsHandler {
    private final TagGame plugin;
    private final Map<ActionSet, ArrayList<Action>> actionsMap = new EnumMap<>(ActionSet.class);

    public ActionsHandler(TagGame plugin, YamlConfiguration config) {
        this.plugin = plugin;

        ConfigurationSection actionsSection = config.getConfigurationSection("actions");
        if (actionsSection == null) return;

        for (String actionSetKey : actionsSection.getKeys(false)) {
            if (actionSetKey.equalsIgnoreCase("ANY")) {
                for (ActionSet actionSet : ActionSet.values()) {
                    this.addAction(actionSet, actionsSection.getConfigurationSection(actionSetKey));
                }

            } else {
                addAction(ActionSet.valueOf(actionSetKey), actionsSection.getConfigurationSection(actionSetKey));
            }
        }
    }

    /**
     * Adds an action to the map.
     * @param actionSet The click that should be used to execute the action.
     * @param actionsSection The section with the action data.
     */
    private void addAction(ActionSet actionSet, ConfigurationSection actionsSection) {
        if (actionsSection == null) return;

        for (Map.Entry<String, Object> actionsMap : actionsSection.getValues(false).entrySet()) {
            ConfigurationSection actionSection = (ConfigurationSection) actionsMap.getValue();
            ActionType actionType = ActionType.getFromConfigName(actionSection.getString("type"));

            if (actionType == null) {
                Logger.log(Level.WARNING, "Invalid action type detected in \"" + actionSection.getName() + "\" for click type " + actionSet.name() + actionSection.getString("type"));
                continue;
            }

            Action action = switch (actionType) {
                case MESSAGE -> new MessageAction(actionSection);
                case CONSOLE_COMMAND -> new ConsoleCommandAction(actionSection);
                case PLAYER_COMMAND -> new PlayerCommandAction(actionSection);
                case TITLE -> new TitleAction(actionSection);
                case ACTIONBAR -> new ActionbarAction(plugin, actionSection);
                case PLAY_SOUND -> new SoundAction(actionSection);
                case EFFECT -> new EffectAction(actionSection);
            };

            if (!action.isFormatCorrect()) {
                String missingFields = String.join(", ", action.getMissingFields());
                Logger.log(Level.WARNING, "Your action \"" + actionSection.getName() + "\" is missing necessary fields: " + missingFields);
                continue;
            }

            // The reason why it isn't correct is handled by the action class.
            if (!action.isCorrect()) continue;

            this.actionsMap.computeIfAbsent(actionSet, k -> new ArrayList<>());
            this.actionsMap.get(actionSet).add(action);
        }
    }

    public void runActions(Player player, ActionSet clickType) {
        ArrayList<Action> actionsToRun = actionsMap.get(clickType);
        if (actionsToRun == null) return;

        for (Action action : actionsToRun) {
            if (action.isGlobal()) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    executeAction(action, player, onlinePlayer);
                }
            } else {
                executeAction(action, player, player);
            }
        }
    }

    /**
     * Runs the action for a specific player.
     * @param action The action to run.
     * @param clickedPlayer The player who clicked the display.
     * @param actionPlayer Who to run the action for.
     */
    private void executeAction(Action action, Player clickedPlayer, Player actionPlayer) {
        if (action.getDelay() > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> action.runAction(clickedPlayer, actionPlayer), action.getDelay());
        } else {
            action.runAction(clickedPlayer, actionPlayer);
        }
    }
}

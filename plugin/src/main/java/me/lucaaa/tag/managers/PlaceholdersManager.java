package me.lucaaa.tag.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.StatsManager;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class PlaceholdersManager extends PlaceholderExpansion {
    private final TagGame plugin;

    public PlaceholdersManager(TagGame plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tag";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Lucaaa";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        // tag_arena_<arena>_<property> -> arena is 0 , <arena> is 1 , <property> is 2
        // tag_player_<property> -> player is 0 , <property> is 1
        // tag_player_arena_<property> -> player is 0 , arena is 1 , <property> is 2
        String[] placeholderParts = params.split("_");

        // If the 1st parameter is arena, return the desired property.
        if (Objects.equals(placeholderParts[0], "arena")) {
            if (placeholderParts.length < 3) return "Must provide arena name and property.";
            else return getArenaPlaceholder(placeholderParts[1], placeholderParts[2]);

        // If the 1st parameter is player...
        } else if (Objects.equals(placeholderParts[0], "player")) {
            if (placeholderParts.length < 2)  {
                return "Must provide property.";

            } else {
                // If the 2nd parameter is arena, return the desired property of the arena the player is in.
                if (Objects.equals(placeholderParts[1], "arena")) {
                    if (!player.isOnline()) return "";

                    PlayerData playerData = plugin.getPlayersManager().getPlayerData(Objects.requireNonNull(player.getPlayer()));
                    if (!playerData.isInArena()) return "";
                    else return getArenaPlaceholder(playerData.arena.getName(), placeholderParts[2]);

                    // If the 2nd parameter is anything other than "arena", return the desired property of the player.
                } else {
                    StatsManager stats = plugin.getPlayersManager().getPlayerStats(player.getName());

                    switch (placeholderParts[1]) {
                        case "gamesPlayed" -> {
                            return String.valueOf(stats.getGamesPlayed());
                        }
                        case "timesLost" -> {
                            return String.valueOf(stats.getTimesLost());
                        }
                        case "timesWon" -> {
                            return String.valueOf(stats.getTimesWon());
                        }
                        case "timesTagger" -> {
                            return String.valueOf(stats.getTimesTagger());
                        }
                        case "timesTagged" -> {
                            return String.valueOf(stats.getTimesTagged());
                        }
                        case "timesBeenTagged" -> {
                            return String.valueOf(stats.getTimesBeenTagged());
                        }
                        case "timeTagger" -> {
                            return String.valueOf(stats.getTimeTagger());
                        }
                        default -> {
                            return "Property not found: " + placeholderParts[1];
                        }
                    }
                }
            }

            // If the 1st parameter is not player or arena, return an error message.
        } else {
            return "Placeholder not found: " + placeholderParts[0];
        }
    }

    private String getArenaPlaceholder(String arenaName, String property) {
        if (!plugin.getArenasManager().arenas.containsKey(arenaName)) {
            return "Arena not found: " + arenaName;

        } else {
            Map<String, String> arenaPlaceholders = plugin.getArenasManager().getArena(arenaName).getPlaceholders();
            if (!arenaPlaceholders.containsKey("%" + property + "%")) return "Property not found: "+property;
            else return arenaPlaceholders.get("%" + property + "%");
        }
    }
}
package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.api.enums.ArenaMode;
import me.lucaaa.tag.api.enums.ArenaTime;
import me.lucaaa.tag.utils.Logger;
import me.lucaaa.tag.api.enums.StopCause;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ArenasManager {
    private final TagGame plugin;
    public final Map<String, Arena> arenas = new HashMap<>();

    public ArenasManager(TagGame plugin) {
        this.plugin = plugin;

        // Gets the arenas folder and creates it if it doesn't exist.
        File arenasFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "arenas");
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }

        // If the arenas folder is not empty, load the arenas.
        Objects.requireNonNull(arenasFolder.listFiles());
        for (File file : Objects.requireNonNull(arenasFolder.listFiles())) {
            Logger.log(Level.INFO, "Loading arena " + file.getName().replaceAll(".yml", "") + "...");
            ConfigManager arenaConfig = new ConfigManager(plugin, "arenas" + File.separator + file.getName());
            this.arenas.put(arenaConfig.getFile().getName().replaceAll(".yml", ""), new Arena(plugin, arenaConfig.getFile().getName().replaceAll(".yml", ""), arenaConfig));
        }
    }

    // Creates the arenas. Returns true if it could be created or false if it couldn't.
    public boolean createArena(String name) throws IOException {
        if (arenas.containsKey(name)) {
            return false;
        }

        ConfigManager newArenaConfig = new ConfigManager(plugin, "arenas" + File.separator + name + ".yml");

        // Set properties in the arena file.
        ConfigurationSection waitingAreaSection = newArenaConfig.getConfig().createSection("waiting-area");
        waitingAreaSection.set("enabled", true);
        waitingAreaSection.set("corner1", "0.0;0.0;0.0");
        waitingAreaSection.set("corner2", "0.0;0.0;0.0");
        waitingAreaSection.set("spawns", List.of());

        ConfigurationSection arenaAreaSection = newArenaConfig.getConfig().createSection("arena-area");
        arenaAreaSection.set("corner1", "0.0;0.0;0.0");
        arenaAreaSection.set("corner2", "0.0;0.0;0.0");
        arenaAreaSection.set("spawns", List.of());

        newArenaConfig.getConfig().set("signs", List.of());

        newArenaConfig.getConfig().set("minPlayers", 2);
        newArenaConfig.getConfig().set("maxPlayers", 10);
        newArenaConfig.getConfig().set("taggers", 1);
        newArenaConfig.getConfig().set("timeEnd", 150);

        newArenaConfig.getConfig().set("time", ArenaTime.UNLIMITED.name());
        newArenaConfig.getConfig().set("mode", ArenaMode.HIT.name());

        newArenaConfig.save();
        arenas.put(name, new Arena(plugin, name, newArenaConfig));
        return true;
    }

    // Deletes an arena. Returns true if it could be deleted or false if it couldn't.
    public boolean deleteArena(String name) {
        if (!arenas.containsKey(name)) {
            return false;
        }

        File arenaFileConfig = new ConfigManager(plugin, "arenas" + File.separator + name +".yml").getFile();
        arenaFileConfig.delete();
        arenas.remove(name);
        return true;
    }

    public void stopAllArenas() {
        for (Arena arena : arenas.values()) {
            arena.stopGame(StopCause.RELOAD, false);
        }
    }

    // Gets an arena by name.
    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public TagArena getTagArena(String name) {
        return arenas.get(name);
    }
}
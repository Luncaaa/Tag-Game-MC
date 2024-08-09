package me.lucaaa.tag.managers;

import me.lucaaa.tag.game.Arena;
import org.bukkit.Location;

import java.util.HashMap;

public class SignsManager {
    private final HashMap<Location, Arena> signs = new HashMap<>();

    public void addSign(Location location, Arena arena) {
        signs.put(location, arena);
    }

    public void removeSign(Location location) {
        signs.remove(location);
    }

    public Arena getArena(Location location) {
        return signs.get(location);
    }

    public boolean isJoinSign(Location location) {
        return signs.containsKey(location);
    }
}
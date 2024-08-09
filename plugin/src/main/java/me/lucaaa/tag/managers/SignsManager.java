package me.lucaaa.tag.managers;

import org.bukkit.Location;

import java.util.HashMap;

public class SignsManager {
    public final HashMap<Location, String> signs = new HashMap<>();

    public void addSign(Location location, String arenaName) {
        signs.put(location, arenaName);
    }

    public void removeSign(Location location) {
        signs.remove(location);
    }
}
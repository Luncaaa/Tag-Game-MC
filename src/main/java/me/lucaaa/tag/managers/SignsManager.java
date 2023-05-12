package me.lucaaa.tag.managers;

import org.bukkit.Location;

import java.util.HashMap;

public class SignsManager {
    public final HashMap<Location, String> signs = new HashMap<>();

    public void addSign(Location location, String arenaName) {
        this.signs.put(location, arenaName);
    }

    public void removeSign(Location location) {
        this.signs.remove(location);
    }
}
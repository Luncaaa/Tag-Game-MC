package me.lucaaa.tag.utils;

public enum ArenaMode {
    HIT("placeholders.arena-mode.hit"),
    TNT("placeholders.arena-mode.tnt"),
    TIMED_HIT("placeholders.arena-mode.timed-hit"),
    TIMED_TNT("placeholders.arena-mode.timed-tnt");

    private final String customNameKey;

    ArenaMode(String customNameKey) {
        this.customNameKey = customNameKey;
    }

    public String getCustomNameKey() {
        return this.customNameKey;
    }
}
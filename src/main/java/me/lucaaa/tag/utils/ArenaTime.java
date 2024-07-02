package me.lucaaa.tag.utils;

public enum ArenaTime {
    LIMITED("placeholders.arena-time.limited"),
    UNLIMITED("placeholders.arena-time.unlimited");

    private final String customNameKey;

    ArenaTime(String customNameKey) {
        this.customNameKey = customNameKey;
    }

    public String getCustomNameKey() {
        return this.customNameKey;
    }
}
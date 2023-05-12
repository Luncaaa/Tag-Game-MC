package me.lucaaa.tag.utils;

import me.lucaaa.tag.TagGame;

public enum ArenaTime {
    LIMITED(TagGame.messagesManager.getMessage("placeholders.arena-time.limited", null, null, false)), UNLIMITED(TagGame.messagesManager.getMessage("placeholders.arena-time.unlimited", null, null, false));

    private final String customName;

    ArenaTime(String customName) {
        this.customName = customName;
    }

    public String getCustomName() {
        return this.customName;
    }
}
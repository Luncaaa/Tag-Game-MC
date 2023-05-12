package me.lucaaa.tag.utils;

import me.lucaaa.tag.TagGame;

public enum ArenaMode {
    HIT(TagGame.messagesManager.getMessage("placeholders.arena-mode.hit", null, null, false)), TNT(TagGame.messagesManager.getMessage("placeholders.arena-mode.tnt", null, null, false)),
    TIMED_HIT(TagGame.messagesManager.getMessage("placeholders.arena-mode.timed-hit", null, null, false)), TIMED_TNT(TagGame.messagesManager.getMessage("placeholders.arena-mode.timed-tnt", null, null, false));

    private final String customName;

    ArenaMode(String customName) {
        this.customName = customName;
    }

    public String getCustomName() {
        return this.customName;
    }
}
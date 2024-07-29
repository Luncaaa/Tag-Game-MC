package me.lucaaa.tag.api.enums;

/**
 * The time mode of a {@link me.lucaaa.tag.api.game.TagArena}
 */
public enum ArenaTime {
    /**
     * The arena has a limited amount of time before ending.
     */
    LIMITED("placeholders.arena-time.limited"),

    /**
     * The arena has an unlimited amount of time. It will never end.
     */
    UNLIMITED("placeholders.arena-time.unlimited");

    private final String customNameKey;

    /**
     * @hidden
     * @param customNameKey The key to the custom name.
     */
    ArenaTime(String customNameKey) {
        this.customNameKey = customNameKey;
    }

    /**
     * @hidden
     * @return The key to the custom name.
     */
    public String getCustomNameKey() {
        return this.customNameKey;
    }
}
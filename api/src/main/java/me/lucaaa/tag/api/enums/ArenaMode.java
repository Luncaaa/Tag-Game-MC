package me.lucaaa.tag.api.enums;

/**
 * The mode of a {@link me.lucaaa.tag.api.game.TagArena}
 */
public enum ArenaMode {
    /**
     * Taggers hit other players to tag them.
     * The loser(s) are the tagger(s) still standing when the time finishes.
     */
    HIT("placeholders.arena-mode.hit"),

    /**
     * Taggers throw TNT that tags players affected by the explosion.
     * The loser(s) are the tagger(s) still standing when the time finishes.
     */
    TNT("placeholders.arena-mode.tnt"),

    /**
     * Taggers hit other players to tag them.
     * The loser is the person who has been a tagger for the longest amount of time (even if he isn't one when the game ends)
     */
    TIMED_HIT("placeholders.arena-mode.timed-hit"),

    /**
     * Taggers throw TNT that tags players affected by the explosion.
     * The loser is the person who has been a tagger for the longest amount of time (even if he isn't one when the game ends)
     */
    TIMED_TNT("placeholders.arena-mode.timed-tnt");

    /**
     * The config's custom name key.
     */
    private final String customNameKey;

    /**
     * @hidden
     * @param customNameKey The key to the custom name.
     */
    ArenaMode(String customNameKey) {
        this.customNameKey = customNameKey;
    }

    /**
     * Returns the config's custom name key.
     * @hidden
     * @return The key to the custom name.
     */
    public String getCustomNameKey() {
        return this.customNameKey;
    }
}
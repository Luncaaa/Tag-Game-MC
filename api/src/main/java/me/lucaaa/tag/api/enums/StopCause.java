package me.lucaaa.tag.api.enums;

/**
 * The reason why a {@link me.lucaaa.tag.api.game.TagArena} was stopped.
 */
public enum StopCause {
    /**
     * The plugin was reloaded.
     */
    RELOAD,

    /**
     * The command /tag stop [arena] was run.
     */
    COMMAND,

    /**
     * The time ended or all players left.
     */
    GAME,

    /**
     * The method TagArena#stopGame() was used by a dev.
     */
    API
}
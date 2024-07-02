package me.lucaaa.tag.utils;

public enum StopCause {
    // Reload: when plugin reloads.
    RELOAD,
    // Command: when /tag stop [arena] is used.
    COMMAND,
    // Game: when time ends or all players leave.
    GAME,
    // API: Dev used the method tagArena.stopGame()
    API
}
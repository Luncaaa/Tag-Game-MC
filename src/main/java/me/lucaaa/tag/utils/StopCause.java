package me.lucaaa.tag.utils;

public enum StopCause {
    // Reload: when plugin reloads.
    // Command: when /tag stop [arena] is used.
    // Game: when time ends or all players leave.
    // API: Dev used the method tagArena.stopGame()
    RELOAD, COMMAND, GAME, API
}
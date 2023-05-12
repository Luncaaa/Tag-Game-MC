package me.lucaaa.tag.api.game;

import org.bukkit.entity.Player;

public interface TagPlayer {
    /**
     * Gets the Spigot player.
     *
     * @return The Spigot player.
     */
    Player getPlayer();

    /**
     * Whether the player is setting up an arena or not.
     *
     * @return Whether the player is setting up an arena or not.
     */
    boolean isSettingUpArena();

    /**
     * Gets the arena a player is setting up (/tag setup <arena>)
     *
     * @return The arena that the player is setting up or null if he isn't setting up an arena.
     */
    TagArena getSettingUpArena();

    /**
     * Whether the player is in an arena or not.<br>
     * WARNING - Returns true even if he is in the waiting area.
     *
     * @return Whether the player is in an arena or not.
     */
    boolean isInArena();

    /**
     * Gets the arena a player is in.<br>
     * WARNING - Returns an arena even if he is in the waiting area.
     *
     * @return The arena that the player is playing in or null if he is not in an arena.
     */
    TagArena getArena();

    /**
     * Gets the number of games that the player has played.
     *
     * @return The number of games that the player has played.
     */
    int getGamesPlayed();
    /**
     * Gets the number of games that the player has lost.<br>
     * Increases when a game starts (tagger countdown starts) and includes games the player has left.
     *
     * @return The number of games that the player has lost.
     */
    int getTimesLost();
    /**
     * Gets the number of games that the player has won.<br>
     * Only arenas with limited time increase this number when they end.
     *
     * @return The number of games that the player has won.
     */
    int getTimesWon();
    /**
     * Gets the number of times that the player has been a tagger.<br>
     * Only arenas with limited time increase this number when they end.
     *
     * @return The number of times that the player has been a tagger.
     */
    int getTimesTagger();
    /**
     * Gets the number of times that the player has tagged someone.
     *
     * @return The number of times that the player has tagged someone.
     */
    int getTimesTagged();
    /**
     * Gets the number of times that the player has been tagged.
     *
     * @return The number of times that the player has been tagged.
     */
    int getTimesBeenTagged();
    /**
     * Gets the time in seconds that the player has been a tagger.
     *
     * @return The time in seconds that the player has been a tagger.
     */
    double getTimeTagger();
}
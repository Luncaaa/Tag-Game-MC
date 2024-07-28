package me.lucaaa.tag.api.game;

@SuppressWarnings("unused")
public interface StatsManager {
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
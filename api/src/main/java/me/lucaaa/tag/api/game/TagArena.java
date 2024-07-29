package me.lucaaa.tag.api.game;

import me.lucaaa.tag.api.enums.ArenaMode;
import me.lucaaa.tag.api.enums.ArenaTime;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Stores data about an arena.
 */
@SuppressWarnings("unused")
public interface TagArena {
    /**
     * Gets the name of the arena.
     *
     * @return The name of the arena.
     */
    String getName();

    // -[ Waiting Area ]-
    /**
     * If the waiting area is disabled, players will be sent to the arena area directly instead of going to a waiting area first.
     *
     * @return Whether the waiting area is enabled (true) or not (false).
     */
    boolean isWaitingAreaEnabled();

    /**
     * Enables or disables the waiting area.
     *
     * @param enabled Waiting area enabled or disabled.
     */
    void setWaitingArenaEnabled(boolean enabled);
    // ----------

    // -[ Players limit ]-
    /**
     * Gets the number of minimum players needed to automatically start the game.
     *
     * @return The current number of minimum players needed.
     */
    int getMinPlayers();

    /**
     * Changes the number of minimum players needed to automatically start the game.
     *
     * @param newLimit The new number of minimum players.
     */
    void setMinPlayers(int newLimit);

    /**
     * Gets the number of maximum players needed to automatically start the game.
     *
     * @return The current number of maximum players needed.
     */
    int getMaxPlayers();

    /**
     * Changes the number of maximum players needed to automatically start the game.
     *
     * @param newLimit The new number of maximum players.
     */
    void setMaxPlayers(int newLimit);

    /**
     * Gets the number of taggers that the game has.
     *
     * @return The current number of taggers.
     */
    int getTaggersNumber();

    /**
     * Changes the number of taggers that the game has.
     *
     * @param number The new number of taggers.
     */
    void setTaggersNumber(int number);
    // ----------

    // -[ Time & Mode ]-
    /**
     * Gets the time before the arena ends automatically.
     *
     * @return The time before the arena ends automatically.
     */
    int getTimeEnd();

    /**
     * Changes the time before the arena ends automatically.
     *
     * @param newTime The new time.
     */
    void setTimeEnd(int newTime);

    /**
     * Gets whether the arena time is unlimited or limited.
     *
     * @return UNLIMITED if the arena won't end automatically or LIMITED.
     */
    ArenaTime getArenaTimeMode();

    /**
     * Changes whether the arena time is unlimited or not.
     *
     * @param newTime The new time mode.
     */
    void setArenaTimeMode(ArenaTime newTime);

    /**
     * Gets the mode of the arena.
     *
     * @return The arena's mode.
     */
    ArenaMode getArenaMode();

    /**
     * Changes the mode of the arena.
     *
     * @param newMode The new mode of the arena.
     */
    void setArenaMode(ArenaMode newMode);
    // ----------

    // -[ Game ]-
    /**
     * Joins a player to the arena.
     *
     * @param player The player you want to join.
     */
    void playerJoin(Player player);

    /**
     * Kicks a player from the arena.
     *
     * @param player The player you want to kick.
     * @param tp Whether the player should be teleported to the lobby or his previous position (depends on config.yml) or stay where he is.
     */
    void playerLeave(Player player, boolean tp);

    /** Starts the arena. */
    void startGame();

    /**
     * Stops the arena.
     *
     * @param runCommands Whether commands should be run for winner and losers.
     */
    void stopGame(boolean runCommands);

    /**
     * Gets the players who are taggers.
     *
     * @return The players who are taggers.
     */
    List<TagPlayer> getTaggers();

    /**
     * Gets all the players in the arena.<br>
     * WARNING - If you are using this method in the TagPlayerJoin event, it won't include the player that joined the arena.<br>
     * WARNING - If you are using this method in the TagPlayerLeave event, it won't include the player that left the arena.
     *
     * @return All the players in the arena.
     */
    List<TagPlayer> getPlayers();
    // ----------
}

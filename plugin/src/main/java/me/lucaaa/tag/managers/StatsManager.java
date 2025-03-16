package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class StatsManager implements me.lucaaa.tag.api.game.StatsManager {
    private final String playerName;
    private final TagGame plugin;
    private final DatabaseManager db;
    private final CompletableFuture<Boolean> isPlayerSaved;
    private final CompletableFuture<Void> loading;

    // Stats
    private int gamesPlayed = 0;
    private int timesLost = 0;
    private int timesWon = 0;
    private int timesTagger = 0;
    private int timesBeenTagged = 0;
    private int timesTagged = 0;
    private double timeTagger = 0.0;

    // Temporary data - if the tagged event is cancelled, it will be added to the data. Otherwise, it will be ignored.
    private int savedTimesTagger = 0;
    private int savedTimesTagged = 0;
    private int savedTimesBeenTagged = 0;

    public StatsManager(OfflinePlayer player, TagGame plugin, boolean is3rdParty) {
        this.playerName = player.getName();
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();

        this.isPlayerSaved = CompletableFuture.supplyAsync(() -> db.playerExists(playerName));

        this.loading = isPlayerSaved.thenAcceptAsync(exists -> {
            if (!exists && is3rdParty) return;

            Runnable runnable = () -> db.loadData(this);

            CompletableFuture<Void> saving = plugin.getDatabaseManager().isSaving(playerName);

            CompletableFuture<Void> afterSaving;
            afterSaving = Objects.requireNonNullElseGet(saving, () -> CompletableFuture.completedFuture(null));

            afterSaving.thenRun(() -> {
                if (!exists) {
                    db.createPlayer(playerName).thenRun(runnable);
                } else {
                    runnable.run();
                }
                runnable.run();
            });
        });
    }

    @Override
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void updateGamesPlayed(int add) {
        gamesPlayed += add;
    }

    @Override
    public int getTimesLost() {
        return timesLost;
    }

    public void updateTimesLost(int add) {
        timesLost += add;
    }

    @Override
    public int getTimesWon() {
        return timesWon;
    }

    public void updateTimesWon(int add) {
        timesWon += add;
    }

    @Override
    public int getTimesTagger() {
        return timesTagger;
    }

    public void updateTimesTagger(int add) {
        timesTagger += add;
    }

    @Override
    public int getTimesBeenTagged() {
        return timesBeenTagged;
    }

    public void updateTimesBeenTagged(int add) {
        timesBeenTagged += add;
    }

    @Override
    public int getTimesTagged() {
        return timesTagged;
    }

    public void updateTimesTagged(int add) {
        timesTagged += add;
    }

    @Override
    public double getTimeTagger() {
        return timeTagger;
    }

    public void updateTimeTagger(double add) {
        timeTagger += add;
    }

    public void saveTempData(int timesTagger, int timesTagged, int timesBeenTagged) {
        savedTimesTagger = timesTagger;
        savedTimesTagged = timesTagged;
        savedTimesBeenTagged = timesBeenTagged;
    }

    public void mergeTempData() {
        updateTimesTagger(savedTimesTagger);
        updateTimesTagged(savedTimesTagged);
        updateTimesBeenTagged(savedTimesBeenTagged);
    }

    public void clearTempData() {
        savedTimesTagger = 0;
        savedTimesTagged = 0;
        savedTimesBeenTagged = 0;
    }

    public void saveData(boolean async) {
        Runnable task = () -> db.saveData(this);

        if (async) {
            plugin.getDatabaseManager().addSaving(playerName, CompletableFuture.runAsync(task));
        } else {
            task.run();
        }
    }

    public boolean isPlayerSaved() {
        // Forces the data to load before returning anything.
        // This method will only be called by PAPI or the API,
        // so it will not freeze anything.
        loading.join();
        return isPlayerSaved.join();
    }

    public String getPlayerName() {
        return playerName;
    }
}
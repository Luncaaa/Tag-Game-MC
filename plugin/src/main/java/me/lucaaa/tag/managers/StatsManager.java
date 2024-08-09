package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;

import java.util.concurrent.CompletableFuture;

public class StatsManager implements me.lucaaa.tag.api.game.StatsManager {
    private final String player;
    private final TagGame plugin;
    private final DatabaseManager db;

    // Stats
    private int gamesPlayed;
    private int timesLost;
    private int timesWon;
    private int timesTagger;
    private int timesBeenTagged;
    private int timesTagged;
    private double timeTagger;

    // Temporary data - if the tagged event is cancelled, it will be added to the data. Otherwise, it will be ignored.
    private int savedTimesTagger = 0;
    private int savedTimesTagged = 0;
    private int savedTimesBeenTagged = 0;

    public StatsManager(String playerName, TagGame plugin) {
        this.player = playerName;
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();

        Runnable runnable = () -> db.createPlayerIfNotExist(playerName).thenRun(() -> {
            gamesPlayed = db.getInt(player, "games_played");
            timesLost = db.getInt(player, "times_lost");
            timesWon = db.getInt(player, "times_won");
            timesTagger = db.getInt(player, "times_tagger");
            timesBeenTagged = db.getInt(player, "times_been_tagged");
            timesTagged = db.getInt(player, "times_tagged");
            timeTagger = db.getDouble(player, "time_tagger");
        });

        CompletableFuture<Void> saving = plugin.getDatabaseManager().isSaving(playerName);
        if (saving != null) {
            saving.thenRun(runnable);
        } else {
            CompletableFuture.runAsync(runnable);
        }
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
        Runnable task = () -> {
            db.updateInt(player, "games_played", gamesPlayed);
            db.updateInt(player, "times_lost", timesLost);
            db.updateInt(player, "times_won", timesWon);
            db.updateInt(player, "times_tagger", timesTagger);
            db.updateInt(player, "times_been_tagged", timesBeenTagged);
            db.updateInt(player, "times_tagged", timesTagged);
            db.updateDouble(player, "time_tagger", timeTagger);
        };

        if (async) {
            plugin.getDatabaseManager().addSaving(player, CompletableFuture.runAsync(task));
        } else {
            task.run();
        }
    }
}
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

        Runnable runnable = () -> {
            db.createPlayerIfNotExist(playerName);

            gamesPlayed = db.getInt(player, "games_played");
            timesLost = db.getInt(player, "times_lost");
            timesWon = db.getInt(player, "times_won");
            timesTagger = db.getInt(player, "times_tagger");
            timesBeenTagged = db.getInt(player, "times_been_tagged");
            timesTagged = db.getInt(player, "times_tagged");
            timeTagger = db.getDouble(player, "time_tagger");
        };

        CompletableFuture<Void> saving = plugin.getDatabaseManager().isSaving(playerName);
        if (saving != null) {
            saving.thenRun(runnable);
        } else {
            CompletableFuture.runAsync(runnable);
        }
    }

    @Override
    public int getGamesPlayed() {
        return this.gamesPlayed;
    }

    public void updateGamesPlayed(int add) {
        this.gamesPlayed += add;
    }

    @Override
    public int getTimesLost() {
        return this.timesLost;
    }

    public void updateTimesLost(int add) {
        this.timesLost += add;
    }

    @Override
    public int getTimesWon() {
        return this.timesWon;
    }

    public void updateTimesWon(int add) {
        this.timesWon += add;
    }

    @Override
    public int getTimesTagger() {
        return this.timesTagger;
    }

    public void updateTimesTagger(int add) {
        this.timesTagger += add;
    }

    @Override
    public int getTimesBeenTagged() {
        return this.timesBeenTagged;
    }

    public void updateTimesBeenTagged(int add) {
        this.timesBeenTagged += add;
    }

    @Override
    public int getTimesTagged() {
        return this.timesTagged;
    }

    public void updateTimesTagged(int add) {
        this.timesTagged += add;
    }

    @Override
    public double getTimeTagger() {
        return this.timeTagger;
    }

    public void updateTimeTagger(double add) {
        this.timeTagger += add;
    }

    public void saveTempData(int timesTagger, int timesTagged, int timesBeenTagged) {
        this.savedTimesTagger = timesTagger;
        this.savedTimesTagged = timesTagged;
        this.savedTimesBeenTagged = timesBeenTagged;
    }

    public void mergeTempData() {
        this.updateTimesTagger(this.savedTimesTagger);
        this.updateTimesTagged(this.savedTimesTagged);
        this.updateTimesBeenTagged(this.savedTimesBeenTagged);
    }

    public void clearTempData() {
        this.savedTimesTagger = 0;
        this.savedTimesTagged = 0;
        this.savedTimesBeenTagged = 0;
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
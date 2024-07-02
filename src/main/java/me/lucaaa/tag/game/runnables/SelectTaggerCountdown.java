package me.lucaaa.tag.game.runnables;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SelectTaggerCountdown {
    private final TagGame plugin;
    private final Arena arena;
    private final ArrayList<PlayerData> playersList;
    private BukkitTask timer = null;
    private final HashMap<String, String> placeholders;
    private final Random random = new Random();

    public SelectTaggerCountdown(TagGame plugin, Arena arena, ArrayList<PlayerData> playersList) {
        this.plugin = plugin;
        this.arena = arena;
        this.playersList = playersList;
        this.placeholders = arena.getPlaceholders();
    }

    public boolean isRunning() {
        return this.timer != null;
    }

    public void start() {
        this.timer = new BukkitRunnable() {
            private int countdown = 7;

            @Override
            public void run() {
                if (countdown <= 5 && countdown >= 1) {
                    placeholders.put("%time%", String.valueOf(countdown));
                    for (PlayerData playerData : playersList) {
                        playerData.getPlayer().sendMessage(plugin.getMessagesManager().getMessage("game.selecting-tagger", placeholders, playerData.getPlayer()));
                    }
                }

                // Selects a random player to be the one who has to tag others
                if (countdown == 0) {
                    // Each random tagger selected is removed to prevent selecting 2 times the same person
                    ArrayList<PlayerData> availableTaggers = new ArrayList<>(playersList);
                    // The taggers the arena will start with
                    ArrayList<PlayerData> taggers = new ArrayList<>();
                    for (int i = 1; i <= arena.getTaggersNumber(); i++) {
                        PlayerData randomTagger;
                        if (availableTaggers.size() == 1) randomTagger = availableTaggers.get(0);
                        else randomTagger = availableTaggers.get(random.nextInt(availableTaggers.size()));

                        availableTaggers.remove(randomTagger);
                        taggers.add(randomTagger);
                    }
                    arena.setInitialTaggers(taggers);
                    stop();
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (this.timer != null) this.timer.cancel();
        this.timer = null;
    }
}
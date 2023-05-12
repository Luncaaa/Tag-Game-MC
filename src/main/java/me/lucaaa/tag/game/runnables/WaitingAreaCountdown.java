package me.lucaaa.tag.game.runnables;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class WaitingAreaCountdown {
    private final Arena arena;
    private final ArrayList<PlayerData> playersList;
    private final ArrayList<Location> spawnsList;
    private BukkitTask timer = null;
    private final HashMap<String, String> placeholders;
    private final Random random = new Random();

    public WaitingAreaCountdown(Arena arena, ArrayList<PlayerData> playersList, ArrayList<Location> spawnsList) {
        this.arena = arena;
        this.playersList = playersList;
        this.spawnsList = spawnsList;
        this.placeholders = arena.getPlaceholders();
    }

    public boolean isRunning() {
        return this.timer != null;
    }

    public void start() {
        this.timer = new BukkitRunnable() {
            private int countdown = 15;
            @Override
            public void run() {
                if (countdown == 15 || countdown == 10 || (countdown <= 5 && countdown >= 1)) {
                    placeholders.put("%time%", String.valueOf(countdown));
                    for (PlayerData playerData : playersList) {
                        playerData.getPlayer().sendMessage(TagGame.messagesManager.getMessage("game.game-starting", placeholders, playerData.getPlayer()));
                    }
                }

                // When the countdown reaches 0, teleport the players to the spawns.
                if (countdown == 0) {
                    for (PlayerData playerData : playersList) {
                        if (spawnsList.size() == 1) playerData.getPlayer().teleport(spawnsList.get(0));
                        else playerData.getPlayer().teleport(spawnsList.get(random.nextInt(spawnsList.size())));
                    }
                    arena.startGame();
                    stop();
                }
                countdown--;
            }
        }.runTaskTimer(TagGame.getPlugin(), 0L, 20L);
    }

    public void stop() {
        this.timer.cancel();
        this.timer = null;
    }
}
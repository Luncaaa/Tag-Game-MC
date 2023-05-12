package me.lucaaa.tag.game.runnables;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.utils.StopCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FinishGameCountdown {
    private final Arena arena;
    private BukkitTask timer = null;
    private int timeLeft;

    public FinishGameCountdown(Arena arena) {
        this.arena = arena;
        this.timeLeft = 0;
    }

    public boolean isRunning() {
        return this.timer != null;
    }

    public void start(int finishTime) {
        this.timeLeft = finishTime;
        this.timer = new BukkitRunnable() {
            @Override
            public void run() {
                arena.updateSigns();
                arena.updateScoreboards();
                if (timeLeft == 0) {
                    arena.stopGame(StopCause.GAME, true);
                    stop();
                }
                timeLeft--;
            }
        }.runTaskTimer(TagGame.getPlugin(), 0, 20L);
    }

    public void stop() {
        if (this.timer != null) this.timer.cancel();
        this.timer = null;
    }

    public int getTimeLeft() {
        return this.timeLeft;
    }
}
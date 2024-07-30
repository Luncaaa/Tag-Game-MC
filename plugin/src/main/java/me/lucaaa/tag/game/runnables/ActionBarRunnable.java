package me.lucaaa.tag.game.runnables;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.game.Arena;
import me.lucaaa.tag.game.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionBarRunnable {
    private final TagGame plugin;
    private final Arena arena;
    private final Map<Player, BukkitTask> currentTaggers = new HashMap<>();

    public ActionBarRunnable(TagGame plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    // Called when the initial taggers are selected.
    public void sendToPlayers(List<PlayerData> taggers) {
        for (PlayerData tagger : taggers) {
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    tagger.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, plugin.getMessagesManager().getMessage("game.tagger-actionbar", arena.getPlaceholders(), tagger.getPlayer(), false));
                }
            }.runTaskTimer(plugin, 0L, 20L);

            this.currentTaggers.put(tagger.getPlayer(), task);
        }
    }

    // Called when a player tags another player.
    public void sendToPlayer(Player tagged, Player tagger) {
        this.currentTaggers.get(tagger).cancel();
        tagger.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        this.currentTaggers.remove(tagger);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                tagged.spigot().sendMessage(ChatMessageType.ACTION_BAR, plugin.getMessagesManager().getMessage("game.tagger-actionbar", arena.getPlaceholders(), tagged.getPlayer(), false));
            }
        }.runTaskTimer(plugin, 0L, 20L);

        this.currentTaggers.put(tagged, task);
    }

    public void stop() {
        for (Map.Entry<Player, BukkitTask> entry : this.currentTaggers.entrySet()) {
            entry.getValue().cancel();
            entry.getKey().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
        }
        this.currentTaggers.clear();
    }
}
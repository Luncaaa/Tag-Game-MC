package me.lucaaa.tag.game;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.game.TagArena;
import me.lucaaa.tag.api.game.TagPlayer;
import me.lucaaa.tag.managers.ItemsManager;
import me.lucaaa.tag.managers.StatsManager;
import me.lucaaa.tag.api.enums.ArenaMode;
import me.lucaaa.tag.api.enums.ArenaTime;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PlayerData implements TagPlayer {
    private final TagGame plugin;
    private final Player player;
    private final StatsManager stats;

    public Long interactEventCooldown = 0L;
    public Long tntThrowCooldown = 0L;
    public Arena arena = null;
    public boolean inWaitingArea = false;
    public Long startTaggerTime = 0L;

    // Saved inventories
    private ItemStack[] savedInventoryContents = null;
    private ItemStack[] savedInventoryArmor = null;
    private ItemStack[] savedInventoryExtra = null;
    private GameMode savedGamemode = null;
    private int savedXPLvl = 0;
    private float savedXPToNextLvl = 0.0F;
    private double savedHealth = 0.0;
    private int savedHunger = 0;
    private boolean dataIsSaved = false;

    // Saved location
    private Location savedLocation = null;

    // Arena setup
    public Arena settingUpArena = null;

    public PlayerData(TagGame plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.stats = new StatsManager(player.getName(), plugin);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean isSettingUpArena() {
        return this.settingUpArena != null;
    }

    @Override
    public TagArena getSettingUpArena() {
        return this.settingUpArena;
    }

    @Override
    public boolean isInArena() {
        return this.arena != null;
    }

    @Override
    public TagArena getArena() {
        return this.arena;
    }

    public Location getSavedLocation() {
        return this.savedLocation;
    }

    // -[ Inventory ]-
    public void saveData() {
        this.savedInventoryContents = this.player.getInventory().getContents();
        this.savedInventoryArmor = this.player.getInventory().getArmorContents();
        this.savedInventoryExtra = this.player.getInventory().getExtraContents();
        this.savedGamemode = this.player.getGameMode();
        this.savedXPLvl = this.player.getLevel();
        this.savedXPToNextLvl = this.player.getExp();
        this.savedHealth = this.player.getHealth();
        this.savedHunger = this.player.getFoodLevel();
        this.savedLocation = this.player.getLocation();
        this.dataIsSaved = true;
    }

    // Replaces current inventory with the saved one
    public void restoreSavedData() {
        if (!this.dataIsSaved) return;
        this.player.getInventory().setContents(this.savedInventoryContents);
        this.player.getInventory().setArmorContents(this.savedInventoryArmor);
        this.player.getInventory().setExtraContents(this.savedInventoryExtra);
        this.player.setGameMode(this.savedGamemode);
        this.player.setLevel(this.savedXPLvl);
        this.player.setExp(this.savedXPToNextLvl);
        this.player.setHealth(this.savedHealth);
        this.player.setFoodLevel(this.savedHunger);

        this.savedInventoryContents = null;
        this.savedInventoryArmor = null;
        this.savedInventoryExtra = null;
        this.savedGamemode = null;
        this.savedXPLvl = 0;
        this.savedXPToNextLvl = 0.0F;
        this.savedHealth = 0.0;
        this.savedHunger = 0;
        this.savedLocation = null;
        this.dataIsSaved = false;
    }

    // Gives inventory with setup tools
    public void giveSetupInventory() {
        this.saveData();
        this.player.getInventory().clear();

        List<String> cornersLore = Arrays.asList("Left click to set corner 1", "Right click to set corner 2");
        List<String> spawnsLore = Arrays.asList("Left click to set a spawn", "Right click to remove a spawn");

        ItemStack arenaAreaCornerAxe = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta arenaAreaCornerAxeMeta = arenaAreaCornerAxe.getItemMeta();
        assert arenaAreaCornerAxeMeta != null;
        arenaAreaCornerAxeMeta.setDisplayName("Set arena corners");
        arenaAreaCornerAxeMeta.setLore(cornersLore);
        arenaAreaCornerAxe.setItemMeta(arenaAreaCornerAxeMeta);

        ItemStack arenaAreaSpawnHoe = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta arenaAreaSpawnHoeMeta = arenaAreaSpawnHoe.getItemMeta();
        assert arenaAreaSpawnHoeMeta != null;
        arenaAreaSpawnHoeMeta.setDisplayName("Set arena spawns");
        arenaAreaCornerAxeMeta.setLore(spawnsLore);
        arenaAreaSpawnHoe.setItemMeta(arenaAreaSpawnHoeMeta);

        ItemStack waitingAreaCornerAxe = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta waitingAreaCornerAxeMeta = waitingAreaCornerAxe.getItemMeta();
        assert waitingAreaCornerAxeMeta != null;
        waitingAreaCornerAxeMeta.setDisplayName("Set waiting area corners");
        arenaAreaCornerAxeMeta.setLore(cornersLore);
        waitingAreaCornerAxe.setItemMeta(waitingAreaCornerAxeMeta);

        ItemStack waitingAreaSpawnHoe = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta waitingAreaSpawnHoeMeta = waitingAreaSpawnHoe.getItemMeta();
        assert waitingAreaSpawnHoeMeta != null;
        waitingAreaSpawnHoeMeta.setDisplayName("Set waiting area spawns");
        arenaAreaCornerAxeMeta.setLore(spawnsLore);
        waitingAreaSpawnHoe.setItemMeta(waitingAreaSpawnHoeMeta);

        this.player.getInventory().setItem(0, arenaAreaCornerAxe);
        this.player.getInventory().setItem(1, arenaAreaSpawnHoe);
        this.player.getInventory().setItem(2, waitingAreaCornerAxe);
        this.player.getInventory().setItem(3, waitingAreaSpawnHoe);
        this.updateSetupInventory();
    }

    // Updates the items in the inventory, such as the waiting area toggle.
    public void updateSetupInventory() {
        this.player.getInventory().setItem(4, this.settingUpArena.getToggleWaitingAreaItem());
        this.player.getInventory().setItem(5, this.settingUpArena.getMinPlayersItem());
        this.player.getInventory().setItem(6, this.settingUpArena.getMaxPlayersItem());
        this.player.getInventory().setItem(7, this.settingUpArena.getArenaTimeModeItem());
        this.player.getInventory().setItem(8, this.settingUpArena.getArenaModeItem());
    }

    // Gives the stick which he can use to tag other players
    public void giveTaggerInventory() {
        ItemsManager itemsManager = plugin.getItemsManager();
        ItemStack taggingStick;
        if (this.arena.getArenaMode() == ArenaMode.HIT || this.arena.getArenaMode() == ArenaMode.TIMED_HIT) {
            if (this.arena.getArenaTimeMode() == ArenaTime.LIMITED) {
                taggingStick = itemsManager.getItem("tag-item-limited");
            } else {
                taggingStick = itemsManager.getItem("tag-item-unlimited");
            }
        } else {
            if (this.arena.getArenaTimeMode() == ArenaTime.LIMITED) {
                taggingStick = itemsManager.getItem("tnt-item-limited");
            } else {
                taggingStick = itemsManager.getItem("tnt-item-unlimited");
            }
        }
        ItemMeta taggingStickMeta = taggingStick.getItemMeta();
        assert taggingStickMeta != null;
        taggingStickMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "TAG"), PersistentDataType.STRING, "tag_stick");
        taggingStick.setItemMeta(taggingStickMeta);

        this.player.getInventory().setItem(0, taggingStick);
        this.player.getInventory().setHelmet(itemsManager.getItem("helmet"));
        this.player.getInventory().setChestplate(itemsManager.getItem("chestplate"));
        this.player.getInventory().setLeggings(itemsManager.getItem("leggings"));
        this.player.getInventory().setBoots(itemsManager.getItem("boots"));
    }
    // ----------

    // -[ Scoreboards ]-
    public void setScoreboard(String name, HashMap<String, String> placeholders) {
        String scoreboardTitle = plugin.getMessagesManager().getMessage("scoreboards." + name + ".title", placeholders, this.player, false, true);

        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective(name, "dummy", scoreboardTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int messagesNumber = plugin.getMessagesManager().getMessagesList("scoreboards."+name+".lines").size();
        for (int index = 0; index < messagesNumber; index++) {
            Score scoreboardLine = objective.getScore(plugin.getMessagesManager().getMessageFromList("scoreboards."+name+".lines", index, placeholders, this.player));
            scoreboardLine.setScore(messagesNumber - index);
        }

        this.player.setScoreboard(scoreboard);
    }
    // ----------

    // -[ Stats ]-
    @Override
    public StatsManager getStatsManager() {
        return this.stats;
    }
    // ----------
}
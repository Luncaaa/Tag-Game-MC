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
import org.bukkit.scoreboard.*;

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
        return player;
    }

    @Override
    public boolean isSettingUpArena() {
        return settingUpArena != null;
    }

    @Override
    public TagArena getSettingUpArena() {
        return settingUpArena;
    }

    @Override
    public boolean isInArena() {
        return arena != null;
    }

    @Override
    public TagArena getArena() {
        return arena;
    }

    public Location getSavedLocation() {
        return savedLocation;
    }

    // -[ Inventory ]-
    public void saveData() {
        savedInventoryContents = player.getInventory().getContents();
        savedInventoryArmor = player.getInventory().getArmorContents();
        savedInventoryExtra = player.getInventory().getExtraContents();
        savedGamemode = player.getGameMode();
        savedXPLvl = player.getLevel();
        savedXPToNextLvl = player.getExp();
        savedHealth = player.getHealth();
        savedHunger = player.getFoodLevel();
        savedLocation = player.getLocation();
        dataIsSaved = true;
    }

    // Replaces current inventory with the saved one
    public void restoreSavedData() {
        if (!dataIsSaved) return;
        player.getInventory().setContents(savedInventoryContents);
        player.getInventory().setArmorContents(savedInventoryArmor);
        player.getInventory().setExtraContents(savedInventoryExtra);
        player.setGameMode(savedGamemode);
        player.setLevel(savedXPLvl);
        player.setExp(savedXPToNextLvl);
        player.setHealth(savedHealth);
        player.setFoodLevel(savedHunger);

        savedInventoryContents = null;
        savedInventoryArmor = null;
        savedInventoryExtra = null;
        savedGamemode = null;
        savedXPLvl = 0;
        savedXPToNextLvl = 0.0F;
        savedHealth = 0.0;
        savedHunger = 0;
        savedLocation = null;
        dataIsSaved = false;
    }

    // Gives inventory with setup tools
    public void giveSetupInventory() {
        saveData();
        player.getInventory().clear();

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

        player.getInventory().setItem(0, arenaAreaCornerAxe);
        player.getInventory().setItem(1, arenaAreaSpawnHoe);
        player.getInventory().setItem(2, waitingAreaCornerAxe);
        player.getInventory().setItem(3, waitingAreaSpawnHoe);
        updateSetupInventory();
    }

    // Updates the items in the inventory, such as the waiting area toggle.
    public void updateSetupInventory() {
        player.getInventory().setItem(4, settingUpArena.getToggleWaitingAreaItem());
        player.getInventory().setItem(5, settingUpArena.getMinPlayersItem());
        player.getInventory().setItem(6, settingUpArena.getMaxPlayersItem());
        player.getInventory().setItem(7, settingUpArena.getArenaTimeModeItem());
        player.getInventory().setItem(8, settingUpArena.getArenaModeItem());
    }

    // Gives the stick which he can use to tag other players
    public void giveTaggerInventory() {
        ItemsManager itemsManager = plugin.getItemsManager();
        ItemStack taggingStick;
        if (arena.getArenaMode() == ArenaMode.HIT || arena.getArenaMode() == ArenaMode.TIMED_HIT) {
            if (arena.getArenaTimeMode() == ArenaTime.LIMITED) {
                taggingStick = itemsManager.getItem("tag-item-limited");
            } else {
                taggingStick = itemsManager.getItem("tag-item-unlimited");
            }
        } else {
            if (arena.getArenaTimeMode() == ArenaTime.LIMITED) {
                taggingStick = itemsManager.getItem("tnt-item-limited");
            } else {
                taggingStick = itemsManager.getItem("tnt-item-unlimited");
            }
        }
        ItemMeta taggingStickMeta = taggingStick.getItemMeta();
        assert taggingStickMeta != null;
        taggingStickMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "TAG"), PersistentDataType.STRING, "tag_stick");
        taggingStick.setItemMeta(taggingStickMeta);

        player.getInventory().setItem(0, taggingStick);
        player.getInventory().setHelmet(itemsManager.getItem("helmet"));
        player.getInventory().setChestplate(itemsManager.getItem("chestplate"));
        player.getInventory().setLeggings(itemsManager.getItem("leggings"));
        player.getInventory().setBoots(itemsManager.getItem("boots"));
    }
    // ----------

    // -[ Scoreboards ]-
    public void setScoreboard(String name, HashMap<String, String> placeholders) {
        String scoreboardTitle = plugin.getMessagesManager().getParsedMessage("scoreboards." + name + ".title", placeholders, player, false);

        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective(name, "dummy", scoreboardTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int messagesNumber = plugin.getMessagesManager().getMessagesList("scoreboards."+name+".lines").size();
        for (int index = 0; index < messagesNumber; index++) {
            Team team = scoreboard.registerNewTeam("TagGameMC-"+name+index);
            team.setSuffix(plugin.getMessagesManager().getMessageFromList("scoreboards."+name+".lines", index, placeholders, player));
            String uniqueEntry = ChatColor.values()[index] + "";
            team.addEntry(uniqueEntry);
            objective.getScore(uniqueEntry).setScore(messagesNumber - index);
        }

        player.setScoreboard(scoreboard);
    }
    // ----------

    // -[ Stats ]-
    @Override
    public StatsManager getStatsManager() {
        return stats;
    }
    // ----------
}
package me.lucaaa.tag.game;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.api.events.*;
import me.lucaaa.tag.api.game.*;
import me.lucaaa.tag.game.runnables.*;
import me.lucaaa.tag.managers.ConfigManager;
import me.lucaaa.tag.api.enums.*;
import me.lucaaa.tag.utils.Logger;
import me.lucaaa.tag.api.enums.StopCause;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Sign;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Arena implements TagArena {
    private final TagGame plugin;
    
    private final String name;
    private final HashMap<String, String> placeholders;
    private final YamlConfiguration arenaConfig;
    private final File arenaFile;

    private World world;
    private Location waitingCorner1;
    private Location waitingCorner2;
    private Location arenaCorner1;
    private Location arenaCorner2;

    private boolean waitingAreaEnabled;

    private final List<Location> waitingAreaSpawns = new ArrayList<>();
    private final List<Location> arenaAreaSpawns = new ArrayList<>();

    private final ArrayList<Location> signs = new ArrayList<>();

    private int minPlayers;
    private int maxPlayers;
    private int taggersNumber;
    private int timeBeforeEnding;

    private ArenaTime arenaTime;
    private ArenaMode arenaMode;

    private final List<PlayerData> playersList = new ArrayList<>();
    private final List<PlayerData> taggers = new ArrayList<>();
    private final HashMap<PlayerData, Double> timeBeingTagger = new HashMap<>();

    private final WaitingAreaCountdown waitingAreaCountdown;
    private final SelectTaggerCountdown selectTaggerCountdown;
    private final FinishGameCountdown finishGameCountdown;
    private final ActionBarRunnable actionBarRunnable;

    private boolean isRunning = false;

    public Arena(TagGame plugin, String name, ConfigManager arenaConfig) {
        this.plugin = plugin;
        
        this.name = name;
        this.arenaConfig = arenaConfig.getConfig();
        this.arenaFile = arenaConfig.getFile();

        this.world = Bukkit.getWorld(this.arenaConfig.get("world", "").toString());

        String[] waitingCorner1Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getString("corner1")).split(";");
        String[] waitingCorner2Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getString("corner2")).split(";");
        this.waitingCorner1 = new Location(this.world, Double.parseDouble(waitingCorner1Coords[0]), Double.parseDouble(waitingCorner1Coords[1]), Double.parseDouble(waitingCorner1Coords[2]));
        this.waitingCorner2 = new Location(this.world, Double.parseDouble(waitingCorner2Coords[0]), Double.parseDouble(waitingCorner2Coords[1]), Double.parseDouble(waitingCorner2Coords[2]));

        String[] arenaCorner1Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).getString("corner1")).split(";");
        String[] arenaCorner2Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).getString("corner2")).split(";");
        this.arenaCorner1 = new Location(this.world, Double.parseDouble(arenaCorner1Coords[0]), Double.parseDouble(arenaCorner1Coords[1]), Double.parseDouble(arenaCorner1Coords[2]));
        this.arenaCorner2 = new Location(this.world, Double.parseDouble(arenaCorner2Coords[0]), Double.parseDouble(arenaCorner2Coords[1]), Double.parseDouble(arenaCorner2Coords[2]));

        this.waitingAreaEnabled = Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getBoolean("enabled");

        for (String sign : this.arenaConfig.getStringList("signs")) {
            String[] coordParts = sign.split(";");
            this.addSign(new Location(Bukkit.getWorld(coordParts[0]), Double.parseDouble(coordParts[1]), Double.parseDouble(coordParts[2]), Double.parseDouble(coordParts[3])));
        }

        this.minPlayers = this.arenaConfig.getInt("minPlayers");
        this.maxPlayers = this.arenaConfig.getInt("maxPlayers");
        this.taggersNumber = this.arenaConfig.getInt("taggers");
        this.timeBeforeEnding = this.arenaConfig.getInt("timeEnd");

        this.arenaTime = ArenaTime.valueOf(this.arenaConfig.getString("time"));
        this.arenaMode = ArenaMode.valueOf(this.arenaConfig.getString("mode"));

        for (String waitingAreaSpawn : Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getStringList("spawns")) {
            String[] coordParts = waitingAreaSpawn.split(";");
            this.waitingAreaSpawns.add(new Location(this.getWorld(), Double.parseDouble(coordParts[0]), Double.parseDouble(coordParts[1]), Double.parseDouble(coordParts[2])));
        }

        for (String arenaAreaSpawn : Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).getStringList("spawns")) {
            String[] coordParts = arenaAreaSpawn.split(";");
            this.arenaAreaSpawns.add(new Location(this.getWorld(), Double.parseDouble(coordParts[0]), Double.parseDouble(coordParts[1]), Double.parseDouble(coordParts[2])));
        }

        this.waitingAreaCountdown = new WaitingAreaCountdown(plugin, this, this.playersList, this.arenaAreaSpawns);
        this.selectTaggerCountdown = new SelectTaggerCountdown(plugin, this, this.playersList);
        this.finishGameCountdown = new FinishGameCountdown(plugin, this);
        this.actionBarRunnable = new ActionBarRunnable(plugin, this);

        this.placeholders = this.getPlaceholders();
        //this.updateSigns()
    }

    @Override
    public String getName() {
        return this.name;
    }

    public HashMap<String, String> getPlaceholders() {
        String finishTime;
        if (this.arenaTime == ArenaTime.UNLIMITED) finishTime = String.valueOf(this.timeBeforeEnding);
        else finishTime = plugin.getMessagesManager().getUncoloredMessage(this.arenaTime.getCustomNameKey(), null, null, false);

        String timeLeft;
        if (this.arenaTime == ArenaTime.UNLIMITED) {
            timeLeft = plugin.getMessagesManager().getUncoloredMessage("placeholders.time.unlimited", null, null, false);
        } else {
            // (this.finishGameCountdown != null) -> false when the getPlaceholders() function is called from the constructor.
            if (this.finishGameCountdown != null && this.finishGameCountdown.isRunning()) timeLeft = String.valueOf(this.finishGameCountdown.getTimeLeft());
            else timeLeft = plugin.getMessagesManager().getUncoloredMessage("placeholders.time.waiting", null, null, false);
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", this.name);
        placeholders.put("%minPlayers%", String.valueOf(this.minPlayers));
        placeholders.put("%maxPlayers%", String.valueOf(this.maxPlayers));
        placeholders.put("%taggersNumber%", String.valueOf(this.taggersNumber));
        placeholders.put("%currentPlayers%", String.valueOf(this.playersList.size()));
        placeholders.put("%time_mode%", plugin.getMessagesManager().getUncoloredMessage(this.arenaTime.getCustomNameKey(), null, null, false));
        placeholders.put("%mode%", plugin.getMessagesManager().getUncoloredMessage(this.arenaMode.getCustomNameKey(), null, null, false));
        placeholders.put("%finishTime%", String.valueOf(finishTime));
        placeholders.put("%taggers%", this.taggers.stream().map(it -> it.getPlayer().getName()).collect(Collectors.joining(", ")));
        placeholders.put("%finishGameCountdown%", timeLeft);

        return placeholders;
    }

    // -[ Signs ]-
    public void addSign(Location location) {
        this.signs.add(location);
        plugin.getSignsManager().addSign(location, this.name);
        this.updateConfigSigns();
    }

    public void removeSign(Location location) {
        this.signs.remove(location);
        plugin.getSignsManager().removeSign(location);
        this.updateConfigSigns();
    }

    private void updateConfigSigns() {
        ArrayList<String> signsList = new ArrayList<>();
        for (Location sign : this.signs) {
            signsList.add(Objects.requireNonNull(sign.getWorld()).getName() + ";" + sign.getX() + ";" + sign.getY() + ";" + sign.getZ());
        }
        this.arenaConfig.set("signs", signsList);
        this.save();
    }

    public void updateSigns() {
        for (Location location : this.signs) {
            if (!(this.world.getBlockAt(location).getState() instanceof Sign sign)) {
                Logger.log(Level.WARNING, "A location inside the signs array in the arena config file \""+this.arenaFile.getName()+"\" is not a sign. Please, check if the coords are valid.");
                continue;
            }
            for (int index = 0; index < sign.getLines().length; index++) {
                sign.setLine(index, plugin.getMessagesManager().getMessageFromList("signs", index, this.getPlaceholders(), null));
            }
            sign.update();
        }
    }
    // ----------

    // - [ World ]-
    private World getWorld() {
        return this.world;
    }

    public void setWorld(String worldName) {
        this.world = Bukkit.getWorld(worldName);
        this.arenaConfig.set("world", worldName);
        this.save();
    }
    // ----------

    // -[ Corners ]-
    public Location getWaitingCorner1() {
        return this.waitingCorner1;
    }
    public void setWaitingCorner1(Location location) {
        this.waitingCorner1 = location;
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).set("corner1", location.getX() + ";" + location.getY() + ";" + location.getZ());
        this.save();
    }

    public Location getWaitingCorner2() {
        return this.waitingCorner2;
    }
    public void setWaitingCorner2(Location location) {
        this.waitingCorner2 = location;
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).set("corner2", location.getX() + ";" + location.getY() + ";" + location.getZ());
        this.save();
    }

    public Location getArenaCorner1() {
        return this.arenaCorner1;
    }
    public void setArenaCorner1(Location location) {
        this.arenaCorner1 = location;
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).set("corner1", location.getX() + ";" + location.getY() + ";" + location.getZ());
        this.save();
    }

    public Location getArenaCorner2() {
        return this.arenaCorner2;
    }
    public void setArenaCorner2(Location location) {
        this.arenaCorner2 = location;
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).set("corner2", location.getX() + ";" + location.getY() + ";" + location.getZ());
        this.save();
    }
    // ----------

    // -[ Waiting Area ]-
    @Override
    public boolean isWaitingAreaEnabled() {
        return this.waitingAreaEnabled;
    }

    @Override
    public void setWaitingArenaEnabled(boolean enabled) {
        this.waitingAreaEnabled = enabled;
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).set("enabled", enabled);
        this.save();
    }

    // Returns a different item depending on whether the arena the user is editing has waiting area enabled or disabled.
    public ItemStack getToggleWaitingAreaItem() {
        ItemStack toggleWaitingAreaItem;
        String toggleWaitingAreaName;

        if (this.isWaitingAreaEnabled()) {
            toggleWaitingAreaItem = new ItemStack(Material.LIME_DYE);
            toggleWaitingAreaName = ChatColor.translateAlternateColorCodes('&', "Waiting area: &aenabled");
        } else {
            toggleWaitingAreaItem = new ItemStack(Material.RED_DYE);
            toggleWaitingAreaName = ChatColor.translateAlternateColorCodes('&', "Waiting area: &cdisabled");
        }

        ItemMeta toggleWaitingAreaMeta = toggleWaitingAreaItem.getItemMeta();
        assert toggleWaitingAreaMeta != null;
        toggleWaitingAreaMeta.setDisplayName(toggleWaitingAreaName);
        toggleWaitingAreaMeta.setLore(Arrays.asList("If enabled, when a player joins this arena,", "he will go to the waiting area first", "until there are enough people to start."));
        toggleWaitingAreaItem.setItemMeta(toggleWaitingAreaMeta);

        return toggleWaitingAreaItem;
    }
    // ----------

    // -[ Spawns ]-
    public boolean addWaitingAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (this.waitingAreaSpawns.contains(blockToAdd)) return false;
        this.waitingAreaSpawns.add(blockToAdd);
        this.updateWaitingAreaSpawns();
        return true;
    }

    public boolean removeWaitingAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (!this.waitingAreaSpawns.contains(blockToAdd)) return false;
        this.waitingAreaSpawns.remove(blockToAdd);
        this.updateWaitingAreaSpawns();
        return true;
    }

    public boolean addArenaAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (this.arenaAreaSpawns.contains(blockToAdd)) return false;
        this.arenaAreaSpawns.add(blockToAdd);
        this.updateArenaAreaSpawns();
        return true;
    }

    public boolean removeArenaAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (!this.arenaAreaSpawns.contains(blockToAdd)) return false;
        this.arenaAreaSpawns.remove(blockToAdd);
        this.updateArenaAreaSpawns();
        return true;
    }

    private void updateWaitingAreaSpawns() {
        ArrayList<String> spawnsList = new ArrayList<>();
        for (Location waitingAreaSpawn : this.waitingAreaSpawns) {
            spawnsList.add(waitingAreaSpawn.getX() + ";" + waitingAreaSpawn.getY() + ";" + waitingAreaSpawn.getZ());
        }
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).set("spawns", spawnsList);
        this.save();
    }

    private void updateArenaAreaSpawns() {
        ArrayList<String> spawnsList = new ArrayList<>();
        for (Location waitingAreaSpawn : this.arenaAreaSpawns) {
            spawnsList.add(waitingAreaSpawn.getX() + ";" + waitingAreaSpawn.getY() + ";" + waitingAreaSpawn.getZ());
        }
        Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).set("spawns", spawnsList);
        this.save();
    }
    // ----------

    // -[ Players limit ]-
    @Override
    public int getMinPlayers() {
        return this.minPlayers;
    }

    @Override
    public void setMinPlayers(int newLimit) {
        if (newLimit <= 0) return;
        this.minPlayers = newLimit;
        this.arenaConfig.set("minPlayers", newLimit);
        this.save();
        this.updateSigns();
    }

    public ItemStack getMinPlayersItem() {
        ItemStack minPlayersItem = new ItemStack(Material.REDSTONE);
        ItemMeta minPlayerMeta = minPlayersItem.getItemMeta();

        assert minPlayerMeta != null;
        minPlayerMeta.setDisplayName("Minimum players: "+this.minPlayers);
        minPlayerMeta.setLore(Arrays.asList("Right click increase", "Shift + Right click to decrease"));

        minPlayersItem.setItemMeta(minPlayerMeta);
        return minPlayersItem;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Override
    public void setMaxPlayers(int newLimit) {
        if (newLimit <= 0) return;
        this.maxPlayers = newLimit;
        this.arenaConfig.set("maxPlayers", newLimit);
        this.save();
        this.updateSigns();
    }

    public ItemStack getMaxPlayersItem() {
        ItemStack maxPlayersItem = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta maxPlayerMeta = maxPlayersItem.getItemMeta();

        assert maxPlayerMeta != null;
        maxPlayerMeta.setDisplayName("Maximum players: "+this.maxPlayers);
        maxPlayerMeta.setLore(Arrays.asList("Right click increase", "Shift + Right click to decrease"));

        maxPlayersItem.setItemMeta(maxPlayerMeta);
        return maxPlayersItem;
    }

    @Override
    public int getTaggersNumber() {
        return this.taggersNumber;
    }

    @Override
    public void setTaggersNumber(int number) {
        if (number <= 0) return;
        this.taggersNumber = number;
        this.arenaConfig.set("taggers", number);
        this.save();
        this.updateSigns();
    }
    // ----------

    // -[ Time & Mode ]-
    @Override
    public int getTimeEnd() {
        return this.timeBeforeEnding;
    }

    @Override
    public void setTimeEnd(int newTime) {
        this.timeBeforeEnding = newTime;
        this.arenaConfig.set("timeEnd", newTime);
        this.save();
    }

    @Override
    public ArenaTime getArenaTimeMode() {
        return this.arenaTime;
    }

    @Override
    public void setArenaTimeMode(ArenaTime newTime) {
        this.arenaTime = newTime;
        this.arenaConfig.set("time", newTime.name());
        this.save();
    }

    public ItemStack getArenaTimeModeItem() {
        ItemStack arenaTimeItem = new ItemStack(Material.CLOCK);
        ItemMeta arenaTimeItemMeta = arenaTimeItem.getItemMeta();
        assert arenaTimeItemMeta != null;

        if (this.arenaTime == ArenaTime.LIMITED) {
            arenaTimeItemMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            arenaTimeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        arenaTimeItemMeta.setDisplayName("Arena time: " + plugin.getMessagesManager().getParsedMessage(this.arenaTime.getCustomNameKey(), null, null, false));
        arenaTimeItemMeta.setLore(Arrays.asList("If the time is limited, after x time the tagger will lose.", "If the time is unlimited, the game will never end.", "", "Note: The game can end before time if a player", "leaves and there are not enough players."));
        arenaTimeItem.setItemMeta(arenaTimeItemMeta);

        return arenaTimeItem;
    }

    @Override
    public ArenaMode getArenaMode() {
        return this.arenaMode;
    }

    @Override
    public void setArenaMode(ArenaMode newMode) {
        this.arenaMode = newMode;
        this.arenaConfig.set("mode", newMode.name());
        this.save();
    }

    public ItemStack getArenaModeItem() {
        ItemStack arenaModeItem;
        if (this.arenaMode == ArenaMode.HIT || this.arenaMode == ArenaMode.TIMED_HIT) arenaModeItem = new ItemStack(Material.DIAMOND_SWORD);
        else arenaModeItem = new ItemStack(Material.TNT);

        ItemMeta arenaModeItemMeta = arenaModeItem.getItemMeta();
        assert arenaModeItemMeta != null;
        if (this.arenaMode == ArenaMode.TIMED_HIT || this.arenaMode == ArenaMode.TIMED_TNT) {
            arenaModeItemMeta.addEnchant(Enchantment.MENDING, 1 , false);
            arenaModeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        arenaModeItemMeta.setDisplayName("Arena mode: " + plugin.getMessagesManager().getParsedMessage(this.arenaMode.getCustomNameKey(), null, null, false));
        arenaModeItemMeta.setLore(Arrays.asList("If the mode is hit, players will have to attack to tag", "If the mode is TNT, players will have to throw TNT to tag.", "If the mode is timed, the player who has been tagger for longer will lose.", "", "Note: Damage won't be dealt while in-game"));
        arenaModeItem.setItemMeta(arenaModeItemMeta);

        return arenaModeItem;
    }
    // ----------

    // -[ Game ]-
    @Override
    public void playerJoin(Player player) {
        if (this.playersList.size() >= this.maxPlayers) {
            plugin.getMessagesManager().sendMessage("commands.arena-full", this.placeholders, player);
            return;
        }

        if (this.isWaitingAreaEnabled()) {
            if (this.isRunning) {
                plugin.getMessagesManager().sendMessage("game.in-game", this.placeholders, player);
                return;
            }
            if (!this.sendToWaitingArea(player)) return;
        } else {
            if (!this.sendToGame(player)) return;
        }

        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        player.getInventory().clear();

        ItemStack leaveItem = plugin.getItemsManager().getItem("leave-item");
        ItemMeta leaveItemMeta = leaveItem.getItemMeta();
        assert leaveItemMeta != null;
        leaveItemMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "TAG"), PersistentDataType.STRING, "leave_item");
        leaveItem.setItemMeta(leaveItemMeta);
        player.getInventory().setItem(8, leaveItem);

        player.setLevel(0);
        player.setExp(0F);
        player.setHealth(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
        player.setFoodLevel(20);
        playerData.arena = this;

        this.updateSigns();
        this.updateScoreboards();
        plugin.getMessagesManager().sendMessage("commands.joined-arena", this.placeholders, player);

        HashMap<String, String> joinPlaceholders = new HashMap<>(this.placeholders);
        joinPlaceholders.put("%player%", player.getName());
        for (PlayerData playerDataInGame : this.playersList) {
            if (playerDataInGame == plugin.getPlayersManager().getPlayerData(player)) return;
            plugin.getMessagesManager().sendMessage("game.player-join", joinPlaceholders, playerDataInGame.getPlayer());
        }
    }

    @Override
    public void playerLeave(Player player, boolean tp) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);

        if (tp) {
            if (plugin.getMainConfig().getConfig().getBoolean("tp-to-lobby")) {
                if (plugin.getMainConfig().getConfig().getLocation("lobby") == null) {
                    Logger.log(Level.WARNING, "The main lobby is not set but \"tp-to-lobby\" is true. The player has been teleported to his previous location.");
                    player.teleport(playerData.getSavedLocation());

                } else {
                    player.teleport(Objects.requireNonNull(plugin.getMainConfig().getConfig().getLocation("lobby")));
                }
            }
            else player.teleport(playerData.getSavedLocation());
        }
        playerData.inWaitingArea = false;
        if (this.arenaTime == ArenaTime.LIMITED) {
            this.timeBeingTagger.remove(playerData);
        } else {
            double taggingTime = (System.currentTimeMillis() - playerData.startTaggerTime) / 1000.0;
            playerData.getStatsManager().updateTimeTagger(taggingTime);
            playerData.startTaggerTime = 0L;
        }
        this.playersList.remove(playerData);
        playerData.restoreSavedData();
        playerData.arena = null;
        playerData.tntThrowCooldown = 0L;
        player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
        plugin.getMessagesManager().sendMessage("commands.left-arena", this.placeholders, player);

        HashMap<String, String> leavePlaceholders = new HashMap<>(this.placeholders);
        leavePlaceholders.put("%player%", player.getName());
        for (PlayerData playerDataInGame : this.playersList) {
            plugin.getMessagesManager().sendMessage("game.player-leave", leavePlaceholders, playerDataInGame.getPlayer());
        }

        if (this.playersList.size() < this.minPlayers) {
            for (PlayerData playerDataInGame : this.playersList) {
                plugin.getMessagesManager().sendMessage("game.not-enough-players", this.placeholders, playerDataInGame.getPlayer());
            }

            if (this.isWaitingAreaEnabled()) {
                if (this.waitingAreaCountdown.isRunning()) this.waitingAreaCountdown.stop();
                if (this.isRunning) this.stopGame(StopCause.GAME, true);

            } else {
                if (this.selectTaggerCountdown.isRunning()) this.selectTaggerCountdown.stop();
                if (this.isRunning) {
                    this.isRunning = false;
                    if (!this.taggers.isEmpty()) this.taggers.forEach(it -> {
                        it.getPlayer().getInventory().setArmorContents(null);
                        it.getPlayer().getInventory().setItem(0, new ItemStack(Material.AIR));
                    });
                    if (this.finishGameCountdown.isRunning()) this.finishGameCountdown.stop();
                    this.actionBarRunnable.stop();
                    this.taggers.clear();
                }
            }

        } else if (this.taggers.contains(playerData)) {
            double taggingTime = (System.currentTimeMillis() - playerData.startTaggerTime) / 1000.0;
            playerData.getStatsManager().updateTimeTagger(taggingTime);
            playerData.startTaggerTime = 0L;

            if (this.arenaMode == ArenaMode.TNT) playerData.tntThrowCooldown = 0L;
            // A player is valid if they are not a tagger
            ArrayList<PlayerData> validPlayers = new ArrayList<>(this.playersList.stream().filter(it -> !this.taggers.contains(it)).toList());
            PlayerData randomPlayer;
            if (validPlayers.size() == 1) randomPlayer = validPlayers.get(0);
            else randomPlayer = validPlayers.get(new Random().nextInt(validPlayers.size()));
            this.setTagger(playerData, randomPlayer);
        }
        PlayerLeaveEvent leaveEvent = new PlayerLeaveEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(leaveEvent);
        this.updateSigns();
        this.updateScoreboards();
    }

    // Sends the player to the waiting lobby. Once there are enough players, the game will start.
    private boolean sendToWaitingArea(Player player) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        if (this.waitingAreaSpawns.isEmpty()) {
            plugin.getMessagesManager().sendMessage("commands.no-spawns", this.placeholders, player);
            return false;
        }

        PlayerJoinEvent joinEvent = new PlayerJoinEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) return false;

        playerData.saveData();
        player.setGameMode(GameMode.ADVENTURE);
        playerData.inWaitingArea = true;
        this.playersList.add(playerData);

        Location randomSpawn;
        if (this.waitingAreaSpawns.size() == 1) randomSpawn = this.waitingAreaSpawns.get(0);
        else randomSpawn = this.waitingAreaSpawns.get(new Random().nextInt(this.waitingAreaSpawns.size()));
        player.teleport(randomSpawn);

        // If there are the minimum players required, start the game and send them to the arena.
        if (this.playersList.size() >= this.minPlayers && !this.waitingAreaCountdown.isRunning()) {
            this.waitingAreaCountdown.start();
        }

        return true;
    }

    // Sends the player to the arena if the waiting area is disabled.
    private boolean sendToGame(Player player) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        if (this.arenaAreaSpawns.isEmpty()) {
            plugin.getMessagesManager().sendMessage("commands.no-spawns", this.placeholders, player);
            return false;
        }

        PlayerJoinEvent joinEvent = new PlayerJoinEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) return false;

        playerData.saveData();
        player.setGameMode(GameMode.ADVENTURE);
        this.playersList.add(playerData);

        Location randomSpawn;
        if (this.arenaAreaSpawns.size() == 1) randomSpawn = this.arenaAreaSpawns.get(0);
        else randomSpawn = this.arenaAreaSpawns.get(new Random().nextInt(this.arenaAreaSpawns.size()));
        player.teleport(randomSpawn);

        if (this.playersList.size() >= this.minPlayers && !this.isRunning) this.startGame();
        return true;
    }

    // Starts the game
    @Override
    public void startGame() {
        ArenaStartEvent startEvent = new ArenaStartEvent(this);
        Bukkit.getPluginManager().callEvent(startEvent);

        for (PlayerData playerData : this.playersList) {
            playerData.getStatsManager().updateGamesPlayed(1);
            playerData.inWaitingArea = false;
        }
        this.isRunning = true;
        this.selectTaggerCountdown.start();

        if (this.arenaTime == ArenaTime.LIMITED) {
            this.finishGameCountdown.start(this.timeBeforeEnding);
        }

        this.updateSigns();
        this.updateScoreboards();
    }

    @Override
    public void stopGame(boolean runCommands) {
        this.stopGame(StopCause.API, runCommands);
    }

    public void stopGame(StopCause cause, boolean runCommands) {
        ArenaStopEvent stopEvent = new ArenaStopEvent(this, cause);
        Bukkit.getPluginManager().callEvent(stopEvent);

        if (this.waitingAreaCountdown.isRunning()) this.waitingAreaCountdown.stop();
        if (this.selectTaggerCountdown.isRunning()) this.selectTaggerCountdown.stop();
        if (this.finishGameCountdown.isRunning()) this.finishGameCountdown.stop();
        this.actionBarRunnable.stop();
        this.isRunning = false;

        if (cause == StopCause.RELOAD) {
            for (PlayerData playerData : this.playersList) {
                if (plugin.getMainConfig().getConfig().getBoolean("tp-to-lobby")) {
                    if (plugin.getMainConfig().getConfig().getLocation("lobby") == null) {
                        Logger.log(Level.WARNING, "The main lobby is not set but \"tp-to-lobby\" is true. The player has been teleported to his previous location.");
                        playerData.getPlayer().teleport(playerData.getSavedLocation());

                    } else {
                        playerData.getPlayer().teleport(Objects.requireNonNull(plugin.getMainConfig().getConfig().getLocation("lobby")));
                    }
                }
                else playerData.getPlayer().teleport(playerData.getSavedLocation());

                playerData.restoreSavedData();
                playerData.getPlayer().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
            }

            this.playersList.clear();
            this.taggers.clear();
            this.updateSigns();
            return;
        }

        ConfigurationSection commandsOnEnd = plugin.getMainConfig().getConfig().getConfigurationSection("commands-on-end");
        assert commandsOnEnd != null;
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();

        ArrayList<PlayerData> losers = new ArrayList<>();
        if (this.arenaMode == ArenaMode.TIMED_HIT || this.arenaMode == ArenaMode.TIMED_TNT) {
            // Get tagger's start tagging time and see for how long he has been tagging. Add that time to the map.
            for (PlayerData tagger : this.taggers) {
                Double taggingTime = (System.currentTimeMillis() - tagger.startTaggerTime) / 1000.0;
                if (this.timeBeingTagger.get(tagger) == null) this.timeBeingTagger.put(tagger, taggingTime);
                else this.timeBeingTagger.replace(tagger, this.timeBeingTagger.get(tagger) + taggingTime);
                tagger.getStatsManager().updateTimeTagger(taggingTime);
            }

            PlayerData highestPlayer = null;
            var highestTime = 0.0;
            for (Map.Entry<PlayerData, Double> entry : this.timeBeingTagger.entrySet()) {
                if (entry.getValue() < highestTime) continue;
                highestTime = entry.getValue();
                highestPlayer = entry.getKey();
            }

            losers.add(highestPlayer);

        } else {
            losers = new ArrayList<>(this.taggers);
        }
        this.timeBeingTagger.clear();

        for (PlayerData playerData : this.playersList) {
            playerData.startTaggerTime = 0L;
            playerData.tntThrowCooldown = 0L;
            HashMap<String, String> playerPlaceholders = new HashMap<>(this.placeholders);
            playerPlaceholders.put("%player%", playerData.getPlayer().getName());

            if (plugin.getMainConfig().getConfig().getBoolean("tp-to-lobby")) {
                if (plugin.getMainConfig().getConfig().getLocation("lobby") == null) {
                    Logger.log(Level.WARNING, "The main lobby is not set but \"tp-to-lobby\" is true. The player has been teleported to his previous location.");
                    playerData.getPlayer().teleport(playerData.getSavedLocation());

                } else {
                    playerData.getPlayer().teleport(Objects.requireNonNull(plugin.getMainConfig().getConfig().getLocation("lobby")));
                }
            }
            else playerData.getPlayer().teleport(playerData.getSavedLocation());

            playerData.restoreSavedData();
            playerData.arena = null;
            playerData.getPlayer().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
            plugin.getMessagesManager().sendMessage("game.game-end", playerPlaceholders, playerData.getPlayer());

            if (losers.contains(playerData)) {
                playerData.getStatsManager().updateTimesLost(1);
                if (runCommands) {
                    for (String command : commandsOnEnd.getStringList("losers")) {
                        Bukkit.dispatchCommand(consoleSender, plugin.getMessagesManager().getColoredMessage(command.replace("%player%", playerData.getPlayer().getName()), this.getPlaceholders(), playerData.getPlayer(), false));
                    }
                }
                plugin.getMessagesManager().sendMessage("game.lose", playerPlaceholders, playerData.getPlayer());
            } else {
                playerData.getStatsManager().updateTimesWon(1);
                if (runCommands) {
                    for (String command : commandsOnEnd.getStringList("winners")) {
                        Bukkit.dispatchCommand(consoleSender, plugin.getMessagesManager().getColoredMessage(command.replace("%player%", playerData.getPlayer().getName()), this.getPlaceholders(), playerData.getPlayer(), false));
                    }
                }
                plugin.getMessagesManager().sendMessage("game.win", playerPlaceholders, playerData.getPlayer());
            }
        }

        this.playersList.clear();
        this.taggers.clear();
        this.updateSigns();
    }

    @Override
    public List<TagPlayer> getTaggers() {
        return new ArrayList<>(this.taggers);
    }

    @Override
    public List<TagPlayer> getPlayers() {
        return new ArrayList<>(this.playersList);
    }

    public void setInitialTaggers(List<PlayerData> taggers) {
        HashMap<String, String> taggersPlaceholders = new HashMap<>(this.placeholders);
        taggersPlaceholders.put("%taggers%", taggers.stream().map(it -> it.getPlayer().getName()).collect(Collectors.joining(", ")));

        this.actionBarRunnable.sendToPlayers(taggers);
        for (PlayerData tagger : taggers) {
            tagger.getStatsManager().saveTempData(1, 0, 0);
            this.taggers.add(tagger);
            tagger.giveTaggerInventory();
            tagger.startTaggerTime = System.currentTimeMillis();
            plugin.getMessagesManager().sendMessage("game.selected-tagger", taggersPlaceholders, tagger.getPlayer());
        }

        for (PlayerData playerData : this.playersList) {
            if (this.taggers.contains(playerData)) continue;
            if (this.taggersNumber > 1) plugin.getMessagesManager().sendMessage("game.selected-taggers-announcement", taggersPlaceholders, playerData.getPlayer());
            else plugin.getMessagesManager().sendMessage("game.selected-tagger-announcement", taggersPlaceholders, playerData.getPlayer());
        }
        this.updateSigns();
        this.updateScoreboards();
    }

    public void setTagger(PlayerData tagger, PlayerData tagged) {
        tagger.getStatsManager().saveTempData(0, 1, 0);
        tagged.getStatsManager().saveTempData(1, 0, 1);

        PlayerTaggedEvent tagEvent = new PlayerTaggedEvent(tagged, tagger, this);
        Bukkit.getPluginManager().callEvent(tagEvent);
        if (tagEvent.isCancelled()) {
            tagged.getStatsManager().clearTempData();
            tagger.getStatsManager().clearTempData();
        }

        tagger.getStatsManager().mergeTempData();
        tagged.getStatsManager().mergeTempData();

        HashMap<String, String> taggedPlaceholders = new HashMap<>(this.placeholders);
        taggedPlaceholders.put("%tagged%", tagged.getPlayer().getName());

        // Get tagger's start tagging time and see for how long he has been tagging. Add that time to the map.
        Double taggingTime = (System.currentTimeMillis() - tagger.startTaggerTime) / 1000.0;
        if (this.timeBeingTagger.get(tagger) == null) this.timeBeingTagger.put(tagger, taggingTime);
        else this.timeBeingTagger.replace(tagger, this.timeBeingTagger.get(tagger) + taggingTime);
        tagger.getStatsManager().updateTimeTagger(taggingTime);
        tagged.startTaggerTime = System.currentTimeMillis();

        this.taggers.remove(tagger);
        taggedPlaceholders.put("%tagger%", tagger.getPlayer().getName());
        tagger.getPlayer().getInventory().setArmorContents(null);
        tagger.getPlayer().getInventory().setItem(0, new ItemStack(Material.AIR));
        plugin.getMessagesManager().sendMessage("game.tagger-tagged", taggedPlaceholders, tagger.getPlayer());
        plugin.getMessagesManager().sendMessage("game.victim-tagged", taggedPlaceholders, tagged.getPlayer());
        for (PlayerData playerData : this.playersList) {
            if (playerData == tagged || playerData == tagger) continue;
            plugin.getMessagesManager().sendMessage("game.tagged-announcement", taggedPlaceholders, playerData.getPlayer());
        }

        this.taggers.add(tagged);
        tagged.giveTaggerInventory();
        this.actionBarRunnable.sendToPlayer(tagged.getPlayer(), tagger.getPlayer());
        this.updateSigns();
        this.updateScoreboards();
    }
    // ----------

    // -[ Utils ]-
    public void checkWorlds(Player player) {
        if (this.getWorld() == null) this.setWorld(player.getWorld().getName());
    }

    public void updateScoreboards() {
        if (!plugin.getMainConfig().getConfig().getBoolean("enable-scoreboards")) return;
        if (this.isRunning) {
            for (PlayerData playerData : this.playersList) {
                if (this.taggers.contains(playerData)) playerData.setScoreboard("tagger", this.getPlaceholders());
                else playerData.setScoreboard("player", this.getPlaceholders());
            }
        } else {
            for (PlayerData playerData : this.playersList) {
                playerData.setScoreboard("waiting", this.getPlaceholders());
            }
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    private void save() {
        try {
            this.arenaConfig.save(this.arenaFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // ----------
}
package me.lucaaa.tag.game;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.actions.ActionSet;
import me.lucaaa.tag.actions.ActionsHandler;
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

    private final ActionsHandler actionsHandler;

    private boolean isRunning = false;

    public Arena(TagGame plugin, String name, ConfigManager arenaConfig) {
        this.plugin = plugin;

        this.name = name;
        this.arenaConfig = arenaConfig.getConfig();
        this.arenaFile = arenaConfig.getFile();

        this.world = Bukkit.getWorld(this.arenaConfig.get("world", "").toString());

        String[] waitingCorner1Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getString("corner1")).split(";");
        String[] waitingCorner2Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getString("corner2")).split(";");
        this.waitingCorner1 = new Location(world, Double.parseDouble(waitingCorner1Coords[0]), Double.parseDouble(waitingCorner1Coords[1]), Double.parseDouble(waitingCorner1Coords[2]));
        this.waitingCorner2 = new Location(world, Double.parseDouble(waitingCorner2Coords[0]), Double.parseDouble(waitingCorner2Coords[1]), Double.parseDouble(waitingCorner2Coords[2]));

        String[] arenaCorner1Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).getString("corner1")).split(";");
        String[] arenaCorner2Coords = Objects.requireNonNull(Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).getString("corner2")).split(";");
        this.arenaCorner1 = new Location(world, Double.parseDouble(arenaCorner1Coords[0]), Double.parseDouble(arenaCorner1Coords[1]), Double.parseDouble(arenaCorner1Coords[2]));
        this.arenaCorner2 = new Location(world, Double.parseDouble(arenaCorner2Coords[0]), Double.parseDouble(arenaCorner2Coords[1]), Double.parseDouble(arenaCorner2Coords[2]));

        this.waitingAreaEnabled = Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getBoolean("enabled");

        for (String sign : this.arenaConfig.getStringList("signs")) {
            String[] coordParts = sign.split(";");
            addSign(new Location(Bukkit.getWorld(coordParts[0]), Double.parseDouble(coordParts[1]), Double.parseDouble(coordParts[2]), Double.parseDouble(coordParts[3])));
        }

        this.minPlayers = this.arenaConfig.getInt("minPlayers");
        this.maxPlayers = this.arenaConfig.getInt("maxPlayers");
        this.taggersNumber = this.arenaConfig.getInt("taggers");
        this.timeBeforeEnding = this.arenaConfig.getInt("timeEnd");

        this.arenaTime = ArenaTime.valueOf(this.arenaConfig.getString("time"));
        this.arenaMode = ArenaMode.valueOf(this.arenaConfig.getString("mode"));

        for (String waitingAreaSpawn : Objects.requireNonNull(this.arenaConfig.getConfigurationSection("waiting-area")).getStringList("spawns")) {
            String[] coordParts = waitingAreaSpawn.split(";");
            this.waitingAreaSpawns.add(new Location(getWorld(), Double.parseDouble(coordParts[0]), Double.parseDouble(coordParts[1]), Double.parseDouble(coordParts[2])));
        }

        for (String arenaAreaSpawn : Objects.requireNonNull(this.arenaConfig.getConfigurationSection("arena-area")).getStringList("spawns")) {
            String[] coordParts = arenaAreaSpawn.split(";");
            this.arenaAreaSpawns.add(new Location(getWorld(), Double.parseDouble(coordParts[0]), Double.parseDouble(coordParts[1]), Double.parseDouble(coordParts[2])));
        }

        this.waitingAreaCountdown = new WaitingAreaCountdown(plugin, this, playersList, arenaAreaSpawns);
        this.selectTaggerCountdown = new SelectTaggerCountdown(plugin, this, playersList);
        this.finishGameCountdown = new FinishGameCountdown(plugin, this);
        this.actionBarRunnable = new ActionBarRunnable(plugin, this);

        if (this.arenaConfig.isConfigurationSection("actions")) {
            this.actionsHandler = new ActionsHandler(plugin, this.arenaConfig);
        } else {
            this.actionsHandler = null;
        }

        this.placeholders = getPlaceholders();
        //updateSigns()
    }

    @Override
    public String getName() {
        return name;
    }

    public HashMap<String, String> getPlaceholders() {
        String finishTime;
        if (arenaTime == ArenaTime.UNLIMITED) finishTime = String.valueOf(timeBeforeEnding);
        else finishTime = plugin.getMessagesManager().getUncoloredMessage(arenaTime.getCustomNameKey(), null, null, false);

        String timeLeft;
        if (arenaTime == ArenaTime.UNLIMITED) {
            timeLeft = plugin.getMessagesManager().getUncoloredMessage("placeholders.time.unlimited", null, null, false);
        } else {
            // (finishGameCountdown != null) -> false when the getPlaceholders() function is called from the constructor.
            if (finishGameCountdown != null && finishGameCountdown.isRunning()) timeLeft = String.valueOf(finishGameCountdown.getTimeLeft());
            else timeLeft = plugin.getMessagesManager().getUncoloredMessage("placeholders.time.waiting", null, null, false);
        }

        HashMap<String, String> placeholders = new HashMap<>();
        placeholders.put("%arena%", name);
        placeholders.put("%minPlayers%", String.valueOf(minPlayers));
        placeholders.put("%maxPlayers%", String.valueOf(maxPlayers));
        placeholders.put("%taggersNumber%", String.valueOf(taggersNumber));
        placeholders.put("%currentPlayers%", String.valueOf(playersList.size()));
        placeholders.put("%time_mode%", plugin.getMessagesManager().getUncoloredMessage(arenaTime.getCustomNameKey(), null, null, false));
        placeholders.put("%mode%", plugin.getMessagesManager().getUncoloredMessage(arenaMode.getCustomNameKey(), null, null, false));
        placeholders.put("%finishTime%", String.valueOf(finishTime));
        placeholders.put("%taggers%", taggers.stream().map(it -> it.getPlayer().getName()).collect(Collectors.joining(", ")));
        placeholders.put("%finishGameCountdown%", timeLeft);

        return placeholders;
    }

    // -[ Signs ]-
    public void addSign(Location location) {
        signs.add(location);
        plugin.getSignsManager().addSign(location, name);
        updateConfigSigns();
    }

    public void removeSign(Location location) {
        signs.remove(location);
        plugin.getSignsManager().removeSign(location);
        updateConfigSigns();
    }

    private void updateConfigSigns() {
        ArrayList<String> signsList = new ArrayList<>();
        for (Location sign : signs) {
            signsList.add(Objects.requireNonNull(sign.getWorld()).getName() + ";" + sign.getX() + ";" + sign.getY() + ";" + sign.getZ());
        }
        arenaConfig.set("signs", signsList);
        save();
    }

    public void updateSigns() {
        for (Location location : signs) {
            if (!(world.getBlockAt(location).getState() instanceof Sign sign)) {
                Logger.log(Level.WARNING, "A location inside the signs array in the arena config file \""+arenaFile.getName()+"\" is not a sign. Please, check if the coords are valid.");
                continue;
            }
            for (int index = 0; index < sign.getLines().length; index++) {
                sign.setLine(index, plugin.getMessagesManager().getMessageFromList("signs", index, getPlaceholders(), null));
            }
            sign.update();
        }
    }
    // ----------

    // - [ World ]-
    private World getWorld() {
        return world;
    }

    public void setWorld(String worldName) {
        world = Bukkit.getWorld(worldName);
        arenaConfig.set("world", worldName);
        save();
    }
    // ----------

    // -[ Corners ]-
    public Location getWaitingCorner1() {
        return waitingCorner1;
    }
    public void setWaitingCorner1(Location location) {
        waitingCorner1 = location;
        Objects.requireNonNull(arenaConfig.getConfigurationSection("waiting-area")).set("corner1", location.getX() + ";" + location.getY() + ";" + location.getZ());
        save();
    }

    public Location getWaitingCorner2() {
        return waitingCorner2;
    }
    public void setWaitingCorner2(Location location) {
        waitingCorner2 = location;
        Objects.requireNonNull(arenaConfig.getConfigurationSection("waiting-area")).set("corner2", location.getX() + ";" + location.getY() + ";" + location.getZ());
        save();
    }

    public Location getArenaCorner1() {
        return arenaCorner1;
    }
    public void setArenaCorner1(Location location) {
        arenaCorner1 = location;
        Objects.requireNonNull(arenaConfig.getConfigurationSection("arena-area")).set("corner1", location.getX() + ";" + location.getY() + ";" + location.getZ());
        save();
    }

    public Location getArenaCorner2() {
        return arenaCorner2;
    }
    public void setArenaCorner2(Location location) {
        arenaCorner2 = location;
        Objects.requireNonNull(arenaConfig.getConfigurationSection("arena-area")).set("corner2", location.getX() + ";" + location.getY() + ";" + location.getZ());
        save();
    }
    // ----------

    // -[ Waiting Area ]-
    @Override
    public boolean isWaitingAreaEnabled() {
        return waitingAreaEnabled;
    }

    @Override
    public void setWaitingArenaEnabled(boolean enabled) {
        waitingAreaEnabled = enabled;
        Objects.requireNonNull(arenaConfig.getConfigurationSection("waiting-area")).set("enabled", enabled);
        save();
    }

    // Returns a different item depending on whether the arena the user is editing has waiting area enabled or disabled.
    public ItemStack getToggleWaitingAreaItem() {
        ItemStack toggleWaitingAreaItem;
        String toggleWaitingAreaName;

        if (isWaitingAreaEnabled()) {
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
        if (waitingAreaSpawns.contains(blockToAdd)) return false;
        waitingAreaSpawns.add(blockToAdd);
        updateWaitingAreaSpawns();
        return true;
    }

    public boolean removeWaitingAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (!waitingAreaSpawns.contains(blockToAdd)) return false;
        waitingAreaSpawns.remove(blockToAdd);
        updateWaitingAreaSpawns();
        return true;
    }

    public boolean addArenaAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (arenaAreaSpawns.contains(blockToAdd)) return false;
        arenaAreaSpawns.add(blockToAdd);
        updateArenaAreaSpawns();
        return true;
    }

    public boolean removeArenaAreaSpawn(Location location) {
        Location blockToAdd = location.add(0.5, 1.0, 0.5);
        if (!arenaAreaSpawns.contains(blockToAdd)) return false;
        arenaAreaSpawns.remove(blockToAdd);
        updateArenaAreaSpawns();
        return true;
    }

    private void updateWaitingAreaSpawns() {
        ArrayList<String> spawnsList = new ArrayList<>();
        for (Location waitingAreaSpawn : waitingAreaSpawns) {
            spawnsList.add(waitingAreaSpawn.getX() + ";" + waitingAreaSpawn.getY() + ";" + waitingAreaSpawn.getZ());
        }
        Objects.requireNonNull(arenaConfig.getConfigurationSection("waiting-area")).set("spawns", spawnsList);
        save();
    }

    private void updateArenaAreaSpawns() {
        ArrayList<String> spawnsList = new ArrayList<>();
        for (Location waitingAreaSpawn : arenaAreaSpawns) {
            spawnsList.add(waitingAreaSpawn.getX() + ";" + waitingAreaSpawn.getY() + ";" + waitingAreaSpawn.getZ());
        }
        Objects.requireNonNull(arenaConfig.getConfigurationSection("arena-area")).set("spawns", spawnsList);
        save();
    }
    // ----------

    // -[ Players limit ]-
    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public void setMinPlayers(int newLimit) {
        if (newLimit <= 0) return;
        minPlayers = newLimit;
        arenaConfig.set("minPlayers", newLimit);
        save();
        updateSigns();
    }

    public ItemStack getMinPlayersItem() {
        ItemStack minPlayersItem = new ItemStack(Material.REDSTONE);
        ItemMeta minPlayerMeta = minPlayersItem.getItemMeta();

        assert minPlayerMeta != null;
        minPlayerMeta.setDisplayName("Minimum players: "+minPlayers);
        minPlayerMeta.setLore(Arrays.asList("Right click increase", "Shift + Right click to decrease"));

        minPlayersItem.setItemMeta(minPlayerMeta);
        return minPlayersItem;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public void setMaxPlayers(int newLimit) {
        if (newLimit <= 0) return;
        maxPlayers = newLimit;
        arenaConfig.set("maxPlayers", newLimit);
        save();
        updateSigns();
    }

    public ItemStack getMaxPlayersItem() {
        ItemStack maxPlayersItem = new ItemStack(Material.GLOWSTONE_DUST);
        ItemMeta maxPlayerMeta = maxPlayersItem.getItemMeta();

        assert maxPlayerMeta != null;
        maxPlayerMeta.setDisplayName("Maximum players: "+maxPlayers);
        maxPlayerMeta.setLore(Arrays.asList("Right click increase", "Shift + Right click to decrease"));

        maxPlayersItem.setItemMeta(maxPlayerMeta);
        return maxPlayersItem;
    }

    @Override
    public int getTaggersNumber() {
        return taggersNumber;
    }

    @Override
    public void setTaggersNumber(int number) {
        if (number <= 0) return;
        taggersNumber = number;
        arenaConfig.set("taggers", number);
        save();
        updateSigns();
    }
    // ----------

    // -[ Time & Mode ]-
    @Override
    public int getTimeEnd() {
        return timeBeforeEnding;
    }

    @Override
    public void setTimeEnd(int newTime) {
        timeBeforeEnding = newTime;
        arenaConfig.set("timeEnd", newTime);
        save();
    }

    @Override
    public ArenaTime getArenaTimeMode() {
        return arenaTime;
    }

    @Override
    public void setArenaTimeMode(ArenaTime newTime) {
        arenaTime = newTime;
        arenaConfig.set("time", newTime.name());
        save();
    }

    public ItemStack getArenaTimeModeItem() {
        ItemStack arenaTimeItem = new ItemStack(Material.CLOCK);
        ItemMeta arenaTimeItemMeta = arenaTimeItem.getItemMeta();
        assert arenaTimeItemMeta != null;

        if (arenaTime == ArenaTime.LIMITED) {
            arenaTimeItemMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
            arenaTimeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        arenaTimeItemMeta.setDisplayName("Arena time: " + plugin.getMessagesManager().getParsedMessage(arenaTime.getCustomNameKey(), null, null, false));
        arenaTimeItemMeta.setLore(Arrays.asList("If the time is limited, after x time the tagger will lose.", "If the time is unlimited, the game will never end.", "", "Note: The game can end before time if a player", "leaves and there are not enough players."));
        arenaTimeItem.setItemMeta(arenaTimeItemMeta);

        return arenaTimeItem;
    }

    @Override
    public ArenaMode getArenaMode() {
        return arenaMode;
    }

    @Override
    public void setArenaMode(ArenaMode newMode) {
        arenaMode = newMode;
        arenaConfig.set("mode", newMode.name());
        save();
    }

    public ItemStack getArenaModeItem() {
        ItemStack arenaModeItem;
        if (arenaMode == ArenaMode.HIT || arenaMode == ArenaMode.TIMED_HIT) arenaModeItem = new ItemStack(Material.DIAMOND_SWORD);
        else arenaModeItem = new ItemStack(Material.TNT);

        ItemMeta arenaModeItemMeta = arenaModeItem.getItemMeta();
        assert arenaModeItemMeta != null;
        if (arenaMode == ArenaMode.TIMED_HIT || arenaMode == ArenaMode.TIMED_TNT) {
            arenaModeItemMeta.addEnchant(Enchantment.MENDING, 1 , false);
            arenaModeItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        arenaModeItemMeta.setDisplayName("Arena mode: " + plugin.getMessagesManager().getParsedMessage(arenaMode.getCustomNameKey(), null, null, false));
        arenaModeItemMeta.setLore(Arrays.asList("If the mode is hit, players will have to attack to tag", "If the mode is TNT, players will have to throw TNT to tag.", "If the mode is timed, the player who has been tagger for longer will lose.", "", "Note: Damage won't be dealt while in-game"));
        arenaModeItem.setItemMeta(arenaModeItemMeta);

        return arenaModeItem;
    }
    // ----------

    // -[ Game ]-
    @Override
    public void playerJoin(Player player) {
        if (playersList.size() >= maxPlayers) {
            plugin.getMessagesManager().sendMessage("commands.arena-full", placeholders, player);
            return;
        }

        if (isWaitingAreaEnabled()) {
            if (isRunning) {
                plugin.getMessagesManager().sendMessage("game.in-game", placeholders, player);
                return;
            }
            if (!sendToWaitingArea(player)) return;
        } else {
            if (!sendToGame(player)) return;
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

        updateSigns();
        updateScoreboards();
        plugin.getMessagesManager().sendMessage("commands.joined-arena", placeholders, player);

        HashMap<String, String> joinPlaceholders = new HashMap<>(placeholders);
        joinPlaceholders.put("%player%", player.getName());
        for (PlayerData playerDataInGame : playersList) {
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
        if (arenaTime == ArenaTime.LIMITED) {
            timeBeingTagger.remove(playerData);
        } else {
            double taggingTime = (System.currentTimeMillis() - playerData.startTaggerTime) / 1000.0;
            playerData.getStatsManager().updateTimeTagger(taggingTime);
            playerData.startTaggerTime = 0L;
        }
        playersList.remove(playerData);
        playerData.restoreSavedData();
        playerData.arena = null;
        playerData.tntThrowCooldown = 0L;
        player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
        plugin.getMessagesManager().sendMessage("commands.left-arena", placeholders, player);

        HashMap<String, String> leavePlaceholders = new HashMap<>(placeholders);
        leavePlaceholders.put("%player%", player.getName());
        for (PlayerData playerDataInGame : playersList) {
            plugin.getMessagesManager().sendMessage("game.player-leave", leavePlaceholders, playerDataInGame.getPlayer());
        }

        if (playersList.size() < minPlayers) {
            for (PlayerData playerDataInGame : playersList) {
                plugin.getMessagesManager().sendMessage("game.not-enough-players", placeholders, playerDataInGame.getPlayer());
            }

            if (isWaitingAreaEnabled()) {
                if (waitingAreaCountdown.isRunning()) waitingAreaCountdown.stop();
                if (isRunning) stopGame(StopCause.GAME, true);

            } else {
                if (selectTaggerCountdown.isRunning()) selectTaggerCountdown.stop();
                if (isRunning) {
                    isRunning = false;
                    if (!taggers.isEmpty()) taggers.forEach(it -> {
                        it.getPlayer().getInventory().setArmorContents(null);
                        it.getPlayer().getInventory().setItem(0, new ItemStack(Material.AIR));
                    });
                    if (finishGameCountdown.isRunning()) finishGameCountdown.stop();
                    actionBarRunnable.stop();
                    taggers.clear();
                }
            }

        } else if (taggers.contains(playerData)) {
            double taggingTime = (System.currentTimeMillis() - playerData.startTaggerTime) / 1000.0;
            playerData.getStatsManager().updateTimeTagger(taggingTime);
            playerData.startTaggerTime = 0L;

            if (arenaMode == ArenaMode.TNT) playerData.tntThrowCooldown = 0L;
            // A player is valid if they are not a tagger
            ArrayList<PlayerData> validPlayers = new ArrayList<>(playersList.stream().filter(it -> !taggers.contains(it)).toList());
            PlayerData randomPlayer;
            if (validPlayers.size() == 1) randomPlayer = validPlayers.get(0);
            else randomPlayer = validPlayers.get(new Random().nextInt(validPlayers.size()));
            setTagger(playerData, randomPlayer);
        }
        PlayerLeaveEvent leaveEvent = new PlayerLeaveEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(leaveEvent);
        updateSigns();
        updateScoreboards();
    }

    // Sends the player to the waiting lobby. Once there are enough players, the game will start.
    private boolean sendToWaitingArea(Player player) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        if (waitingAreaSpawns.isEmpty()) {
            plugin.getMessagesManager().sendMessage("commands.no-spawns", placeholders, player);
            return false;
        }

        PlayerJoinEvent joinEvent = new PlayerJoinEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) return false;

        playerData.saveData();
        player.setGameMode(GameMode.ADVENTURE);
        playerData.inWaitingArea = true;
        playersList.add(playerData);

        Location randomSpawn;
        if (waitingAreaSpawns.size() == 1) randomSpawn = waitingAreaSpawns.get(0);
        else randomSpawn = waitingAreaSpawns.get(new Random().nextInt(waitingAreaSpawns.size()));
        player.teleport(randomSpawn);

        // If there are the minimum players required, start the game and send them to the arena.
        if (playersList.size() >= minPlayers && !waitingAreaCountdown.isRunning()) {
            waitingAreaCountdown.start();
        }

        return true;
    }

    // Sends the player to the arena if the waiting area is disabled.
    private boolean sendToGame(Player player) {
        PlayerData playerData = plugin.getPlayersManager().getPlayerData(player);
        if (arenaAreaSpawns.isEmpty()) {
            plugin.getMessagesManager().sendMessage("commands.no-spawns", placeholders, player);
            return false;
        }

        PlayerJoinEvent joinEvent = new PlayerJoinEvent(playerData, this);
        Bukkit.getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) return false;

        playerData.saveData();
        player.setGameMode(GameMode.ADVENTURE);
        playersList.add(playerData);

        Location randomSpawn;
        if (arenaAreaSpawns.size() == 1) randomSpawn = arenaAreaSpawns.get(0);
        else randomSpawn = arenaAreaSpawns.get(new Random().nextInt(arenaAreaSpawns.size()));
        player.teleport(randomSpawn);

        if (playersList.size() >= minPlayers && !isRunning) startGame();
        return true;
    }

    // Starts the game
    @Override
    public void startGame() {
        ArenaStartEvent startEvent = new ArenaStartEvent(this);
        Bukkit.getPluginManager().callEvent(startEvent);

        for (PlayerData playerData : playersList) {
            playerData.getStatsManager().updateGamesPlayed(1);
            playerData.inWaitingArea = false;
        }
        isRunning = true;
        selectTaggerCountdown.start();

        if (arenaTime == ArenaTime.LIMITED) {
            finishGameCountdown.start(timeBeforeEnding);
        }

        updateSigns();
        updateScoreboards();
    }

    @Override
    public void stopGame(boolean executeActions) {
        stopGame(StopCause.API, executeActions);
    }

    public void stopGame(StopCause cause, boolean executeActions) {
        ArenaStopEvent stopEvent = new ArenaStopEvent(this, cause);
        Bukkit.getPluginManager().callEvent(stopEvent);

        if (waitingAreaCountdown.isRunning()) waitingAreaCountdown.stop();
        if (selectTaggerCountdown.isRunning()) selectTaggerCountdown.stop();
        if (finishGameCountdown.isRunning()) finishGameCountdown.stop();
        actionBarRunnable.stop();
        isRunning = false;

        if (cause == StopCause.RELOAD) {
            for (PlayerData playerData : playersList) {
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

            playersList.clear();
            taggers.clear();
            updateSigns();
            return;
        }

        ArrayList<PlayerData> losers = new ArrayList<>();
        if (arenaMode == ArenaMode.TIMED_HIT || arenaMode == ArenaMode.TIMED_TNT) {
            // Get tagger's start tagging time and see for how long he has been tagging. Add that time to the map.
            for (PlayerData tagger : taggers) {
                Double taggingTime = (System.currentTimeMillis() - tagger.startTaggerTime) / 1000.0;
                if (timeBeingTagger.get(tagger) == null) timeBeingTagger.put(tagger, taggingTime);
                else timeBeingTagger.replace(tagger, timeBeingTagger.get(tagger) + taggingTime);
                tagger.getStatsManager().updateTimeTagger(taggingTime);
            }

            PlayerData highestPlayer = null;
            var highestTime = 0.0;
            for (Map.Entry<PlayerData, Double> entry : timeBeingTagger.entrySet()) {
                if (entry.getValue() < highestTime) continue;
                highestTime = entry.getValue();
                highestPlayer = entry.getKey();
            }

            losers.add(highestPlayer);

        } else {
            losers = new ArrayList<>(taggers);
        }
        timeBeingTagger.clear();

        for (PlayerData playerData : playersList) {
            playerData.startTaggerTime = 0L;
            playerData.tntThrowCooldown = 0L;
            HashMap<String, String> playerPlaceholders = new HashMap<>(placeholders);
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
            playerData.getStatsManager().saveData(true);

            playerData.getPlayer().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
            plugin.getMessagesManager().sendMessage("game.game-end", playerPlaceholders, playerData.getPlayer());

            ActionsHandler actionsHandler = (this.actionsHandler == null) ? plugin.getActionsHandler() : this.actionsHandler;

            if (losers.contains(playerData)) {
                playerData.getStatsManager().updateTimesLost(1);
                if (executeActions) {
                    actionsHandler.runActions(playerData.getPlayer(), ActionSet.LOSERS);
                }
                plugin.getMessagesManager().sendMessage("game.lose", playerPlaceholders, playerData.getPlayer());
            } else {
                playerData.getStatsManager().updateTimesWon(1);
                if (executeActions) {
                    actionsHandler.runActions(playerData.getPlayer(), ActionSet.WINNERS);
                }
                plugin.getMessagesManager().sendMessage("game.win", playerPlaceholders, playerData.getPlayer());
            }

            playerData.arena = null;
        }

        playersList.clear();
        taggers.clear();
        updateSigns();
    }

    @Override
    public List<TagPlayer> getTaggers() {
        return new ArrayList<>(taggers);
    }

    @Override
    public List<TagPlayer> getPlayers() {
        return new ArrayList<>(playersList);
    }

    public void setInitialTaggers(List<PlayerData> taggers) {
        HashMap<String, String> taggersPlaceholders = new HashMap<>(placeholders);
        taggersPlaceholders.put("%taggers%", taggers.stream().map(it -> it.getPlayer().getName()).collect(Collectors.joining(", ")));

        actionBarRunnable.sendToPlayers(taggers);
        for (PlayerData tagger : taggers) {
            tagger.getStatsManager().saveTempData(1, 0, 0);
            this.taggers.add(tagger);
            tagger.giveTaggerInventory();
            tagger.startTaggerTime = System.currentTimeMillis();
            plugin.getMessagesManager().sendMessage("game.selected-tagger", taggersPlaceholders, tagger.getPlayer());
        }

        for (PlayerData playerData : playersList) {
            if (taggers.contains(playerData)) continue;
            if (taggersNumber > 1) plugin.getMessagesManager().sendMessage("game.selected-taggers-announcement", taggersPlaceholders, playerData.getPlayer());
            else plugin.getMessagesManager().sendMessage("game.selected-tagger-announcement", taggersPlaceholders, playerData.getPlayer());
        }
        updateSigns();
        updateScoreboards();
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

        HashMap<String, String> taggedPlaceholders = new HashMap<>(placeholders);
        taggedPlaceholders.put("%tagged%", tagged.getPlayer().getName());

        // Get tagger's start tagging time and see for how long he has been tagging. Add that time to the map.
        Double taggingTime = (System.currentTimeMillis() - tagger.startTaggerTime) / 1000.0;
        if (timeBeingTagger.get(tagger) == null) timeBeingTagger.put(tagger, taggingTime);
        else timeBeingTagger.replace(tagger, timeBeingTagger.get(tagger) + taggingTime);
        tagger.getStatsManager().updateTimeTagger(taggingTime);
        tagged.startTaggerTime = System.currentTimeMillis();

        taggers.remove(tagger);
        taggedPlaceholders.put("%tagger%", tagger.getPlayer().getName());
        tagger.getPlayer().getInventory().setArmorContents(null);
        tagger.getPlayer().getInventory().setItem(0, new ItemStack(Material.AIR));
        plugin.getMessagesManager().sendMessage("game.tagger-tagged", taggedPlaceholders, tagger.getPlayer());
        plugin.getMessagesManager().sendMessage("game.victim-tagged", taggedPlaceholders, tagged.getPlayer());
        for (PlayerData playerData : playersList) {
            if (playerData == tagged || playerData == tagger) continue;
            plugin.getMessagesManager().sendMessage("game.tagged-announcement", taggedPlaceholders, playerData.getPlayer());
        }

        taggers.add(tagged);
        tagged.giveTaggerInventory();
        actionBarRunnable.sendToPlayer(tagged.getPlayer(), tagger.getPlayer());
        updateSigns();
        updateScoreboards();
    }
    // ----------

    // -[ Utils ]-
    public void checkWorlds(Player player) {
        if (getWorld() == null) setWorld(player.getWorld().getName());
    }

    public void updateScoreboards() {
        if (!plugin.getMainConfig().getConfig().getBoolean("enable-scoreboards")) return;
        if (isRunning) {
            for (PlayerData playerData : playersList) {
                if (taggers.contains(playerData)) playerData.setScoreboard("tagger", getPlaceholders());
                else playerData.setScoreboard("player", getPlaceholders());
            }
        } else {
            for (PlayerData playerData : playersList) {
                playerData.setScoreboard("waiting", getPlaceholders());
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void save() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // ----------
}
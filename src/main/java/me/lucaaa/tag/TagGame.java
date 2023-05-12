package me.lucaaa.tag;

import me.lucaaa.tag.commands.MainCommand;
import me.lucaaa.tag.commands.subCommands.SubCommandsFormat;
import me.lucaaa.tag.listeners.*;
import me.lucaaa.tag.managers.*;
import me.lucaaa.tag.utils.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

public class TagGame extends JavaPlugin {
    // An instance of the plugin.
    private static Plugin plugin;
    private static boolean isPAPIInstalled = false;

    // Subcommands for the HelpSubCommand class.
    public static HashMap<String, SubCommandsFormat> subCommands = MainCommand.subCommands;

    // Config & lang file.
    public static ConfigManager mainConfig;
    private static ConfigManager langConfig;

    // Managers.
    public static DatabaseManager databaseManager;
    public static MessagesManager messagesManager;
    public static SignsManager signsManager;
    public static PlayersManager playersManager;
    public static ItemsManager itemsManager;
    public static ArenasManager arenasManager;

    // Reload the config files.
    public static void reloadConfigs() throws SQLException, IOException {
        // Creates the config and lang files.
        createConfigs();
        mainConfig = new ConfigManager(plugin, "config.yml");

        // Loads the lang file the user wants.
        langConfig = new ConfigManager(plugin, "langs" + File.separator + mainConfig.getConfig().getString("language"));

        // Managers
        databaseManager = new DatabaseManager(mainConfig.getConfig().getBoolean("database.use-mysql"));
        messagesManager = new MessagesManager(langConfig.getConfig());
        signsManager = new SignsManager();
        if (playersManager != null) playersManager.removeEveryone();
        playersManager = new PlayersManager();
        itemsManager = new ItemsManager(mainConfig);
        if (arenasManager != null) arenasManager.stopAllArenas();
        arenasManager = new ArenasManager(plugin);
    }

    // If the config files do not exist, create them.
    private static void createConfigs() {
        if (!new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml").exists())
            plugin.saveResource("config.yml", false);
        if (!new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "langs" + File.separator + "en.yml").exists())
            plugin.saveResource("langs" + File.separator + "en.yml", false);
        if (!new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "langs" + File.separator + "es.yml").exists())
            plugin.saveResource("langs" + File.separator + "es.yml", false);
    }

    // Gets the plugin.
    public static Plugin getPlugin() {
        return plugin;
    }

    // Returns true if PAPI is installed
    public static boolean isPAPIInstalled() {
        return isPAPIInstalled;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Set up files and managers.
        try {
            reloadConfigs();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        // If PAPI is installed, register the placeholders.
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            isPAPIInstalled = true;
            new PlaceholdersManager().register();
        }

        // Register events.
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new RightClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new EntityExplodeListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        // Registers the main command and adds tab completions.
        Objects.requireNonNull(this.getCommand("tag")).setExecutor(new MainCommand());
        Objects.requireNonNull(this.getCommand("tag")).setTabCompleter(new MainCommand());

        Logger.log(Level.INFO, "The plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        // Stops all arenas.
        arenasManager.stopAllArenas();
        // Gives everyone that setting up an arena their saved inventory.
        playersManager.removeEveryone();

        Logger.log(Level.INFO, "The plugin has been disabled.");
    }
}
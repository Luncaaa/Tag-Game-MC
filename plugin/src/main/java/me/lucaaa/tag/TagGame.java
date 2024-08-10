package me.lucaaa.tag;

import me.lucaaa.tag.actions.ActionsHandler;
import me.lucaaa.tag.api.APIImplementation;
import me.lucaaa.tag.api.APIProvider;
import me.lucaaa.tag.commands.MainCommand;
import me.lucaaa.tag.listeners.*;
import me.lucaaa.tag.managers.*;
import me.lucaaa.tag.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class TagGame extends JavaPlugin {
    private boolean isPAPIInstalled = false;

    // Config file.
    private ConfigManager mainConfig;

    // Managers.
    private DatabaseManager databaseManager;
    private MessagesManager messagesManager;
    private SignsManager signsManager;
    private PlayersManager playersManager;
    private ItemsManager itemsManager;
    private ArenasManager arenasManager;

    // Default actions handler.
    private ActionsHandler actionsHandler;

    // Reload the config files.
    public void reloadConfigs() {
        // Creates the config and lang files.
        createConfigs();
        mainConfig = new ConfigManager(this, "config.yml");

        // If it detects the old commands-on-end section, send a warning.
        if (mainConfig.getConfig().isConfigurationSection("commands-on-end")) {
            Logger.log(Level.WARNING, "\"commands-on-end\" section detected. This does not work since v1.3!");
            Logger.log(Level.WARNING, "Update your config.yml (and arena files!) to the new actions system.");
            Logger.log(Level.WARNING, "Learn more at https://lucaaa.gitbook.io/tag-game/usage/actions");
        }

        // Loads the lang file the user wants.
        ConfigManager langConfig = new ConfigManager(this, "langs" + File.separator + mainConfig.getConfig().getString("language"));

        // Managers
        Runnable startDB = () -> {
            try {
                databaseManager = new DatabaseManager(TagGame.this, mainConfig.getConfig().getBoolean("database.use-mysql"));
            } catch (SQLException | IOException e) {
                Logger.logError(Level.SEVERE, "An error occurred while initialising the database manager. Data won't be saved or read.", e);
            }
        };

        if (playersManager != null) {
            playersManager.removeEveryone().thenRun(() -> databaseManager.closePool()).thenRun(startDB);
        } else {
            startDB.run();
        }

        messagesManager = new MessagesManager(langConfig.getConfig(), mainConfig.getConfig().getString("prefix"), this.isPAPIInstalled);
        signsManager = new SignsManager();
        itemsManager = new ItemsManager(this, mainConfig);
        if (arenasManager != null) arenasManager.stopAllArenas();
        arenasManager = new ArenasManager(this);
        playersManager = new PlayersManager(this);

        actionsHandler = new ActionsHandler(this, mainConfig.getConfig());
    }

    // If the config files do not exist, create them.
    private void createConfigs() {
        if (!new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml").exists())
            saveResource("config.yml", false);
        if (!new File(getDataFolder().getAbsolutePath() + File.separator + "langs" + File.separator + "en.yml").exists())
            saveResource("langs" + File.separator + "en.yml", false);
        if (!new File(getDataFolder().getAbsolutePath() + File.separator + "langs" + File.separator + "es.yml").exists())
            saveResource("langs" + File.separator + "es.yml", false);
    }

    @Override
    public void onEnable() {
        // If PAPI is installed, register the placeholders.
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            isPAPIInstalled = true;
            new PlaceholdersManager(this).register();
        }

        // Set up files and managers.
        reloadConfigs();

        // Look for updates.
        new UpdateManager(this).getVersion(v -> UpdateManager.sendStatus(this, v, getDescription().getVersion()));

        // Register events.
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new RightClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityExplodeListener(this), this);

        // Registers the main command and adds tab completions.
        MainCommand commandHandler = new MainCommand(this);
        Objects.requireNonNull(this.getCommand("tag")).setExecutor(commandHandler);
        Objects.requireNonNull(this.getCommand("tag")).setTabCompleter(commandHandler);

        // Enables the API.
        APIProvider.setImplementation(new APIImplementation(this));

        Bukkit.getConsoleSender().sendMessage(messagesManager.getColoredMessage("&aThe plugin has been successfully enabled! &7Version: " + this.getDescription().getVersion(), true));
    }

    @Override
    public void onDisable() {
        // Stops all arenas.
        arenasManager.stopAllArenas();
        // Gives everyone that setting up an arena their saved inventory.
        playersManager.removeEveryone().thenRun(() -> databaseManager.closePool());

        Logger.log(Level.INFO, "The plugin has been disabled.");
    }

    public ConfigManager getMainConfig() {
        return this.mainConfig;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public MessagesManager getMessagesManager() {
        return this.messagesManager;
    }

    public SignsManager getSignsManager() {
        return this.signsManager;
    }

    public PlayersManager getPlayersManager() {
        return this.playersManager;
    }

    public ItemsManager getItemsManager() {
        return this.itemsManager;
    }

    public ArenasManager getArenasManager() {
        return this.arenasManager;
    }

    public ActionsHandler getActionsHandler() {
        return this.actionsHandler;
    }
}
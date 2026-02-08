package me.lucaaa.tag;

import me.lucaaa.tag.actions.ActionsHandler;
import me.lucaaa.tag.api.APIImplementation;
import me.lucaaa.tag.api.APIProvider;
import me.lucaaa.tag.commands.MainCommand;
import me.lucaaa.tag.listeners.*;
import me.lucaaa.tag.managers.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class TagGame extends JavaPlugin {
    private boolean isPAPIInstalled = false;
    public NamespacedKey key = new NamespacedKey(this, "TAG");
    private BukkitAudiences audiences;

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
        mainConfig = new ConfigManager(this, "config.yml", true);

        // If it detects the old commands-on-end section, send a warning.
        if (mainConfig.getConfig().isConfigurationSection("commands-on-end")) {
            log(Level.WARNING, "\"commands-on-end\" section detected. This does not work since v1.3!");
            log(Level.WARNING, "Update your config.yml (and arena files!) to the new actions system.");
            log(Level.WARNING, "Learn more at https://lucaaa.gitbook.io/tag-game/usage/actions");
        }

        ConfigManager.saveResource(this, "langs" + File.separator + "en.yml");
        ConfigManager.saveResource(this, "langs" + File.separator + "es.yml");

        // Loads the lang file the user wants.
        ConfigManager langConfig;
        String language = mainConfig.getConfig().getString("language");
        if (language == null) {
            log(Level.WARNING, "Language setting not specified in config.yml! Defaulting to en.yml");
            langConfig = new ConfigManager(this, "langs" + File.separator + "en.yml", true);

        } else {
            try {
                langConfig = new ConfigManager(this, "langs" + File.separator + language, false);
            } catch(IllegalArgumentException e) {
                log(Level.WARNING, "Language file \"" + language + "\" was not found! Defaulting to en.yml");
                langConfig = new ConfigManager(this, "langs" + File.separator + "en.yml", true);
            }
        }

        // Managers
        Runnable startDB = () -> {
            try {
                databaseManager = new DatabaseManager(TagGame.this, mainConfig.getConfig().getBoolean("database.use-mysql"));
            } catch (SQLException | IOException e) {
                logError(Level.SEVERE, "An error occurred while initialising the database manager. Data won't be saved or read.", e);
            }
        };

        if (playersManager != null) {
            CompletableFuture.runAsync(() -> {
                playersManager.removeEveryone();
                databaseManager.closePool();
                startDB.run();
            });
        } else {
            startDB.run();
        }

        messagesManager = new MessagesManager(this, langConfig.getConfig(), mainConfig.getConfig().getString("prefix"));
        signsManager = new SignsManager();
        itemsManager = new ItemsManager(this, mainConfig);
        if (arenasManager != null) arenasManager.stopAllArenas();
        arenasManager = new ArenasManager(this);
        playersManager = new PlayersManager(this);

        actionsHandler = new ActionsHandler(this, mainConfig.getConfig());
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
        audiences = BukkitAudiences.create(this);

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
        CompletableFuture.runAsync(() -> {
            playersManager.removeEveryone();
            databaseManager.closePool();
        });

        if (audiences != null) audiences.close();

        log(Level.INFO, "The plugin has been disabled.");
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void logError(Level level, String message, Throwable error) {
        getLogger().log(level, message, error);
    }

    public boolean isPAPIInstalled() {
        return isPAPIInstalled;
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

    public Audience getAudience(CommandSender sender) {
        return audiences.sender(sender);
    }
}
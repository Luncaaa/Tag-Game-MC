package me.lucaaa.tag.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lucaaa.tag.TagGame;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {
    private final TagGame plugin;
    private final Map<String, CompletableFuture<Void>> savingData = new HashMap<>();
    private final CompletableFuture<Void> dataSourceInit;
    private boolean dataSourceInitDone = false;

    // Connection pool
    private HikariDataSource dataSource;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DatabaseManager(TagGame plugin, boolean useMySQL) throws IOException, SQLException {
        this.plugin = plugin;

        String url;
        String user;
        String password;

        if (!useMySQL) {
            user = null;
            password = null;
            File dbFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "stats.db");
            if (!dbFile.exists()) dbFile.createNewFile();
            url = "jdbc:sqlite:"+ dbFile.getAbsolutePath();

        } else {
            ConfigurationSection dbConfig = plugin.getMainConfig().getConfig().getConfigurationSection("database.mysql");
            assert dbConfig != null;
            String host = dbConfig.getString("host");
            String port = dbConfig.getString("port");
            user = dbConfig.getString("username");
            password = dbConfig.getString("password");
            String dbName = dbConfig.getString("name");
            url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
        }

        this.dataSourceInit = CompletableFuture.runAsync(() -> {
            setupPool(useMySQL, url, user, password);

            try(Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS player_stats(name TINYTEXT, games_played Int, times_lost Int, times_won Int, times_tagger Int, times_tagged Int, times_been_tagged Int, time_tagger Double)")) {
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.logError(Level.SEVERE, "An error occurred while creating the stats table.", e);
            }
        });
    }

    private void setupPool(boolean useMysql, String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        if (useMysql) {
            config.setJdbcUrl(url);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
        } else {
            config.setJdbcUrl(url);
            config.setDriverClassName("org.sqlite.JDBC");
        }
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(50);
        config.setConnectionTimeout(60000);
        config.setConnectionTestQuery("SELECT 1");
        dataSource = new HikariDataSource(config);
        dataSourceInitDone = true;
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "An error occurred while getting a database connection. Data won't be updated!");
            throw new RuntimeException(e);
        }
    }

    public boolean playerExists(String playerName) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_stats WHERE name = ?")) {
            statement.setString(1, playerName);
            ResultSet results = statement.executeQuery();

            boolean exists = results.next();
            results.close();
            return exists;

        } catch (SQLException e) {
            plugin.logError(Level.SEVERE, "An error occurred while checking if player " + playerName + " exists.", e);
            return false;
        }
    }

    public CompletableFuture<Void> createPlayer(String playerName) {
        Runnable task = () -> {
            try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("INSERT INTO player_stats VALUES ('"+playerName+"', 0, 0, 0, 0, 0, 0, 0.0)")) {
                statement.executeUpdate();

            } catch (SQLException e) {
                plugin.logError(Level.SEVERE, "An error occurred while creating stats for player " + playerName, e);
            }
        };

        if (!dataSourceInitDone) {
            return dataSourceInit.thenRun(task);
        } else {
            return CompletableFuture.runAsync(task);
        }
    }

    public void loadData(StatsManager statsManager) {
        String playerName = statsManager.getPlayerName();
        statsManager.updateGamesPlayed(getInt(playerName, "games_played"));
        statsManager.updateTimesLost(getInt(playerName, "times_lost"));
        statsManager.updateTimesWon(getInt(playerName, "times_won"));
        statsManager.updateTimesTagger(getInt(playerName, "times_tagger"));
        statsManager.updateTimesBeenTagged(getInt(playerName, "times_been_tagged"));
        statsManager.updateTimesTagged(getInt(playerName, "times_tagged"));
        statsManager.updateTimeTagger(getDouble(playerName, "time_tagger"));
    }

    public void saveData(StatsManager statsManager) {
        String playerName = statsManager.getPlayerName();
        updateInt(playerName, "games_played", statsManager.getGamesPlayed());
        updateInt(playerName, "times_lost", statsManager.getTimesLost());
        updateInt(playerName, "times_won", statsManager.getTimesWon());
        updateInt(playerName, "times_tagger", statsManager.getTimesTagger());
        updateInt(playerName, "times_been_tagged", statsManager.getTimesBeenTagged());
        updateInt(playerName, "times_tagged", statsManager.getTimesTagged());
        updateDouble(playerName, "time_tagger", statsManager.getTimeTagger());
    }

    private int getInt(String playerName, String dataToGet) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_stats WHERE name = ?")) {
            statement.setString(1, playerName);
            ResultSet query = statement.executeQuery();
            query.next();
            int result = query.getInt(dataToGet);
            query.close();
            return result;
        } catch (SQLException e) {
            plugin.logError(Level.SEVERE, "An error occurred while getting data \"" + dataToGet + "\" for player " + playerName, e);
            return 0;
        }
    }

    private double getDouble(String playerName, String dataToGet) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_stats WHERE name = ?")) {
            statement.setString(1, playerName);
            ResultSet query = statement.executeQuery();
            query.next();
            double result = query.getDouble(dataToGet);
            query.close();
            return result;
        } catch (SQLException e) {
            plugin.logError(Level.SEVERE, "An error occurred while getting data \"" + dataToGet + "\" for player " + playerName, e);
            return 0.0;
        }
    }

    private void updateInt(String playerName, String dataToUpdate, int newValue) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("UPDATE player_stats SET "+dataToUpdate+" = ? WHERE name = ?")) {
            statement.setInt(1, newValue);
            statement.setString(2, playerName);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.logError(Level.SEVERE, "An error occurred while saving data \"" + dataToUpdate + "\" for player " + playerName, e);
        }
    }

    private void updateDouble(String playerName, String dataToUpdate, double newValue) {
        try (Connection conn = getConnection(); PreparedStatement statement = conn.prepareStatement("UPDATE player_stats SET "+dataToUpdate+" = ? WHERE name = ?")) {
            statement.setDouble(1, newValue);
            statement.setString(2, playerName);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.logError(Level.SEVERE, "An error occurred while saving data \"" + dataToUpdate + "\" for player " + playerName, e);
        }
    }

    public CompletableFuture<Void> isSaving(String playerName) {
        return savingData.get(playerName);
    }

    public void addSaving(String playerName, CompletableFuture<Void> function) {
        function.thenRun(() -> savingData.remove(playerName));
        savingData.put(playerName, function);
    }
}
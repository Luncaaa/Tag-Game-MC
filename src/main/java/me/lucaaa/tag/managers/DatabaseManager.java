package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    // yay!
    private final Connection connection;

    public DatabaseManager(boolean useMySQL) throws IOException, SQLException {
        if (!useMySQL) {
            File dbFile = new File(TagGame.getPlugin().getDataFolder().getAbsolutePath() + File.separator + "stats.db");
            if (!dbFile.exists()) dbFile.createNewFile();
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+dbFile.getAbsolutePath());

        } else {
            ConfigurationSection dbConfig = TagGame.mainConfig.getConfig().getConfigurationSection("database.mysql");
            assert dbConfig != null;
            String host = dbConfig.getString("host");
            String port = dbConfig.getString("port");
            String user = dbConfig.getString("username");
            String password = dbConfig.getString("password");
            String dbName = dbConfig.getString("name");
            this.connection = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+dbName, user, password);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Statement statement = connection.createStatement();
                    statement.execute("CREATE TABLE IF NOT EXISTS player_stats(name TINYTEXT, games_played Int, times_lost Int, times_won Int, times_tagger Int, times_tagged Int, times_been_tagged Int, time_tagger Double)");
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TagGame.getPlugin());
    }

    public boolean playerIsInDB(String playerName) {
        CompletableFuture<Boolean> async = CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
                statement.setString(1, playerName);
                ResultSet results = statement.executeQuery();
                boolean result = results.next();
                results.close();
                return result;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });

        return async.join();
    }

    public void createPlayer(String playerName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO player_stats VALUES ('"+playerName+"', 0, 0, 0, 0, 0, 0, 0.0)");
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TagGame.getPlugin());
    }

    public int getInt(String playerName, String dataToGet) {
        CompletableFuture<Integer> async = CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
                statement.setString(1, playerName);
                ResultSet query = statement.executeQuery();
                query.next();
                int result = query.getInt(dataToGet);
                query.close();
                return result;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });

        return async.join();
    }

    public double getDouble(String playerName, String dataToGet) {
        CompletableFuture<Double> async = CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
                statement.setString(1, playerName);
                ResultSet query = statement.executeQuery();
                query.next();
                double result = query.getDouble(dataToGet);
                query.close();
                return result;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });

        return async.join();
    }

    public void updateInt(String playerName, String dataToUpdate, int newValue) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = connection.prepareStatement("UPDATE player_stats SET "+dataToUpdate+" = ? WHERE name = ?");
                    statement.setInt(1, newValue);
                    statement.setString(2, playerName);
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TagGame.getPlugin());
    }

    public void updateDouble(String playerName, String dataToUpdate, double newValue) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = connection.prepareStatement("UPDATE player_stats SET "+dataToUpdate+" = ? WHERE name = ?");
                    statement.setDouble(1, newValue);
                    statement.setString(2, playerName);
                    statement.executeUpdate();
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TagGame.getPlugin());
    }
}
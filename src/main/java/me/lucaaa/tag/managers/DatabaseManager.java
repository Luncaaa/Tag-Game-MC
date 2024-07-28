package me.lucaaa.tag.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lucaaa.tag.TagGame;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final TagGame plugin;
    private Connection connection;
    private final HashMap<String, CompletableFuture<Void>> savingData = new HashMap<>();

    // Connection pool
    private HikariDataSource dataSource;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DatabaseManager(TagGame plugin, boolean useMySQL) throws IOException, SQLException {
        this.plugin = plugin;

        if (!useMySQL) {
            File dbFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "stats.db");
            if (!dbFile.exists()) dbFile.createNewFile();
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+dbFile.getAbsolutePath());
            createTable(this.connection);

        } else {
            ConfigurationSection dbConfig = plugin.getMainConfig().getConfig().getConfigurationSection("database.mysql");
            assert dbConfig != null;
            String host = dbConfig.getString("host");
            String port = dbConfig.getString("port");
            String user = dbConfig.getString("username");
            String password = dbConfig.getString("password");
            String dbName = dbConfig.getString("name");
            String url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
            setupPool(url, user, password);
            createTable(dataSource.getConnection());
        }
    }

    private void createTable(Connection conn) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS player_stats(name TINYTEXT, games_played Int, times_lost Int, times_won Int, times_tagger Int, times_tagged Int, times_been_tagged Int, time_tagger Double)");
                    statement.executeUpdate();
                    close(conn, statement);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void setupPool(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(username);
        config.setPassword(password);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(50000);
        config.setConnectionTestQuery("SELECT 1");
        dataSource = new HikariDataSource(config);
    }

    private void close(Connection connection, PreparedStatement statement) {
        try {
            if (connection != null && connection != this.connection) {
                connection.close();
            }
            if (statement != null) statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private Connection getConnection() {
        try {
            return (this.connection != null) ? this.connection : dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPlayerIfNotExist(String playerName) {
        try {
            Connection conn = this.getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
            statement.setString(1, playerName);
            ResultSet results = statement.executeQuery();

            if (!results.next()) {
                PreparedStatement addStatement = conn.prepareStatement("INSERT INTO player_stats VALUES ('"+playerName+"', 0, 0, 0, 0, 0, 0, 0.0)");
                addStatement.executeUpdate();
                addStatement.close();
            }

            results.close();
            close(conn, statement);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getInt(String playerName, String dataToGet) {
        try {
            Connection conn = this.getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
            statement.setString(1, playerName);
            ResultSet query = statement.executeQuery();
            query.next();
            int result = query.getInt(dataToGet);
            query.close();
            close(conn, statement);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public double getDouble(String playerName, String dataToGet) {
        try {
            Connection conn = this.getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM player_stats WHERE name = ?");
            statement.setString(1, playerName);
            ResultSet query = statement.executeQuery();
            query.next();
            double result = query.getDouble(dataToGet);
            query.close();
            close(conn, statement);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateInt(String playerName, String dataToUpdate, int newValue) {
        try {
            Connection conn = this.getConnection();
            PreparedStatement statement = conn.prepareStatement("UPDATE player_stats SET "+dataToUpdate+" = ? WHERE name = ?");
            statement.setInt(1, newValue);
            statement.setString(2, playerName);
            statement.executeUpdate();
            close(conn, statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDouble(String playerName, String dataToUpdate, double newValue) {
        try {
            Connection conn = this.getConnection();
            PreparedStatement statement = conn.prepareStatement("UPDATE player_stats SET "+dataToUpdate+" = ? WHERE name = ?");
            statement.setDouble(1, newValue);
            statement.setString(2, playerName);
            statement.executeUpdate();
            close(conn, statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> isSaving(String playerName) {
        return this.savingData.get(playerName);
    }

    public void addSaving(String playerName, CompletableFuture<Void> function) {
        function.thenRun(() -> savingData.remove(playerName));
        this.savingData.put(playerName, function);
    }
}
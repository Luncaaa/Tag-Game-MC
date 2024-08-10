package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateManager {
    private final TagGame plugin;
    private final int RESOURCE_ID = 109807;

    public UpdateManager(TagGame plugin) {
        this.plugin = plugin;
    }

    public void getVersion(Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                InputStream resourcePage = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID + "/~").toURL().openStream();
                Scanner scanner = new Scanner(resourcePage);
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException | URISyntaxException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }

    public static void sendStatus(TagGame plugin, String spigotVersion, String pluginVersion) {
        String[] spigotVerDivided = spigotVersion.split("\\.");
        double spigotVerMajor = Double.parseDouble(spigotVerDivided[0] + "." + spigotVerDivided[1]);
        double spigotVerMinor = (spigotVerDivided.length > 2) ? Integer.parseInt(spigotVerDivided[2]) : 0;

        String[] pluginVerDivided = pluginVersion.split("\\.");
        double pluginVerMajor = Double.parseDouble(pluginVerDivided[0] + "." + pluginVerDivided[1]);
        double pluginVerMinor = (pluginVerDivided.length > 2) ? Integer.parseInt(pluginVerDivided[2]) : 0;

        if (spigotVerMajor == pluginVerMajor && spigotVerMinor == pluginVerMinor) {
            Bukkit.getConsoleSender().sendMessage(plugin.getMessagesManager().getColoredMessage("&aThe plugin is up to date! &7(v" + pluginVersion + ")", true));

        } else if (spigotVerMajor > pluginVerMajor || (spigotVerMajor == pluginVerMajor && spigotVerMinor > pluginVerMinor)) {
            Bukkit.getConsoleSender().sendMessage(plugin.getMessagesManager().getColoredMessage("&6There's a new update available on Spigot! &c" + pluginVersion + " &7-> &a" + spigotVersion, true));
            Bukkit.getConsoleSender().sendMessage(plugin.getMessagesManager().getColoredMessage("&6Download it at &7https://www.spigotmc.org/resources/advanceddisplays.110865/", true));

        } else {
            Bukkit.getConsoleSender().sendMessage(plugin.getMessagesManager().getColoredMessage("&6Your plugin version is newer than the Spigot version! &a" + pluginVersion + " &7-> &c" + spigotVersion, true));
            Bukkit.getConsoleSender().sendMessage(plugin.getMessagesManager().getColoredMessage("&6There may be bugs and/or untested features!", true));
        }
    }
}
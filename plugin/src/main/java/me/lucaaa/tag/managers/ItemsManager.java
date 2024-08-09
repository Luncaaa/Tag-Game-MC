package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import me.lucaaa.tag.utils.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class ItemsManager {
    private final TagGame plugin;
    private final ConfigManager configManager;
    private final HashMap<String, ItemStack> items = new HashMap<>();

    public ItemsManager(TagGame plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;

        for (String item : Objects.requireNonNull(configManager.getConfig().getConfigurationSection("items")).getKeys(false)) {
            this.loadItem("items", item);
        }

        for (String item : Objects.requireNonNull(configManager.getConfig().getConfigurationSection("tagger-inventory")).getKeys(false)) {
            this.loadItem("tagger-inventory", item);
        }

        // Loads item that is used when item is not found
        ItemStack notFound = new ItemStack(Material.BARRIER);
        ItemMeta meta = Objects.requireNonNull(notFound.getItemMeta());
        meta.setDisplayName(ChatColor.RED + "Item not found!");
        meta.setLore(List.of(ChatColor.GRAY + "Check the default config to see what you are missing."));
        notFound.setItemMeta(meta);
        this.items.put("not-found", notFound);
    }

    private void loadItem(String category, String itemConfigSectionName) {
        ConfigurationSection itemConfigSection = configManager.getConfig().getConfigurationSection(category + "." + itemConfigSectionName);

        ItemStack item;

        assert itemConfigSection != null;
        if (itemConfigSection.getString("item") == null) {
            Logger.log(Level.WARNING, "The item type was not specified for the item \"" + itemConfigSection.getName() + "\". Setting to stick by default.");
            item = new ItemStack(Material.STICK);
        } else if ((Material.getMaterial(Objects.requireNonNull(itemConfigSection.getString("item"))) == null)) {
            Logger.log(Level.WARNING, "The item type \"" + itemConfigSection.getString("item") + "\" specified for the item \"" + itemConfigSection.getName() + "\" does not exist. Setting to stick by default.");
            item = new ItemStack(Material.STICK);
        } else {
            item = new ItemStack(Material.valueOf(itemConfigSection.getString("item")));
        }

        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;

        if (itemConfigSection.getString("color") != null && itemMeta instanceof LeatherArmorMeta) {
            String[] color = Objects.requireNonNull(itemConfigSection.getString("color")).split(";");
            ((LeatherArmorMeta) itemMeta).setColor(Color.fromRGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
        }

        if (itemConfigSection.getString("name") != null) itemMeta.setDisplayName(plugin.getMessagesManager().getColoredMessage(itemConfigSection.getString("name"), false));
        if (itemConfigSection.getList("lore") != null) {
            ArrayList<String> lore = new ArrayList<>();
            for (String loreLine : itemConfigSection.getStringList("lore")) {
                lore.add(plugin.getMessagesManager().getColoredMessage(loreLine, false));
            }
            itemMeta.setLore(lore);
        }
        
        if (itemConfigSection.getBoolean("glowing")) {
            itemMeta.addEnchant(Enchantment.DAMAGE_ALL, 5, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(itemMeta);

        items.put(itemConfigSectionName, item);
    }

    public ItemStack getItem(String itemName) {
        return (items.containsKey(itemName)) ? items.get(itemName) : items.get("not-found");
    }
}
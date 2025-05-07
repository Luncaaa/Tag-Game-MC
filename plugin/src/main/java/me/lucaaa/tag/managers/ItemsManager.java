package me.lucaaa.tag.managers;

import me.lucaaa.tag.TagGame;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;
import java.util.logging.Level;

public class ItemsManager {
    private final TagGame plugin;
    private final Map<String, ItemStack> items = new HashMap<>();

    public ItemsManager(TagGame plugin, ConfigManager configManager) {
        this.plugin = plugin;

        YamlConfiguration config = configManager.getConfig();
        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) continue;

            if (key.endsWith("material")) {
                String sectionName = key.substring(0, key.lastIndexOf('.'));
                if (items.containsKey(sectionName)) continue;

                loadItem(sectionName, Objects.requireNonNull(config.getConfigurationSection(sectionName)));
            }
        }

        // Loads item that is used when item is not found
        ItemStack notFound = new ItemStack(Material.BARRIER);
        ItemMeta meta = Objects.requireNonNull(notFound.getItemMeta());
        meta.setDisplayName(ChatColor.RED + "Item not found!");
        meta.setLore(List.of(ChatColor.GRAY + "Check the default config to see what you are missing."));
        notFound.setItemMeta(meta);
        this.items.put("not-found", notFound);
    }

    private void loadItem(String key, ConfigurationSection itemSection) {
        ItemStack item;

        if (itemSection.getString("item") == null) {
            plugin.log(Level.WARNING, "The item type was not specified for the item \"" + itemSection.getName() + "\". Setting to stick by default.");
            item = new ItemStack(Material.STICK);
        } else if ((Material.getMaterial(Objects.requireNonNull(itemSection.getString("item"))) == null)) {
            plugin.log(Level.WARNING, "The item type \"" + itemSection.getString("item") + "\" specified for the item \"" + itemSection.getName() + "\" does not exist. Setting to stick by default.");
            item = new ItemStack(Material.STICK);
        } else {
            item = new ItemStack(Material.valueOf(itemSection.getString("item")));
        }

        ItemMeta itemMeta = Objects.requireNonNull(item.getItemMeta());

        if (itemSection.getString("color") != null && itemMeta instanceof LeatherArmorMeta) {
            String[] color = Objects.requireNonNull(itemSection.getString("color")).split(";");
            ((LeatherArmorMeta) itemMeta).setColor(Color.fromRGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
        }

        if (itemSection.getString("name") != null) itemMeta.setDisplayName(plugin.getMessagesManager().getColoredMessage(itemSection.getString("name"), false));
        if (!itemSection.getStringList("lore").isEmpty()) {
            List<String> lore = new ArrayList<>();
            for (String loreLine : itemSection.getStringList("lore")) {
                lore.add(plugin.getMessagesManager().getColoredMessage(loreLine, false));
            }
            itemMeta.setLore(lore);
        }
        
        if (itemSection.getBoolean("glowing")) {
            itemMeta.addEnchant(Enchantment.MENDING, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(itemMeta);

        items.put(key, item);
    }

    public ItemStack getItem(String itemName) {
        return (items.containsKey(itemName)) ? items.get(itemName) : items.get("not-found");
    }

    public boolean itemExists(String itemName) {
        return items.containsKey(itemName);
    }
}
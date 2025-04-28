package com.orbismc.itemloretowny.manager;

import com.orbismc.itemloretowny.tracker.KillTracker;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ScrollManager {
    private final JavaPlugin plugin;
    private final KillTracker killTracker;
    private final NamespacedKey playerKillScrollKey;
    private final NamespacedKey mobKillScrollKey;

    public ScrollManager(JavaPlugin plugin, KillTracker killTracker) {
        this.plugin = plugin;
        this.killTracker = killTracker;
        this.playerKillScrollKey = new NamespacedKey(plugin, "player_kill_scroll");
        this.mobKillScrollKey = new NamespacedKey(plugin, "mob_kill_scroll");
    }

    public ItemStack createPlayerKillScroll() {
        ItemStack scroll = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = scroll.getItemMeta();

        meta.setDisplayName("§c§lScroll of Player Slaying");
        List<String> lore = new ArrayList<>();
        lore.add("§7Apply this scroll to a weapon in an anvil");
        lore.add("§7to track player kills.");
        meta.setLore(lore);

        // Add a custom tag to identify this as a player kill scroll
        meta.getPersistentDataContainer().set(
                playerKillScrollKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        scroll.setItemMeta(meta);
        return scroll;
    }

    public ItemStack createMobKillScroll() {
        ItemStack scroll = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = scroll.getItemMeta();

        meta.setDisplayName("§a§lScroll of Monster Slaying");
        List<String> lore = new ArrayList<>();
        lore.add("§7Apply this scroll to a weapon in an anvil");
        lore.add("§7to track mob kills.");
        meta.setLore(lore);

        // Add a custom tag to identify this as a mob kill scroll
        meta.getPersistentDataContainer().set(
                mobKillScrollKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        scroll.setItemMeta(meta);
        return scroll;
    }

    public boolean isPlayerKillScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(
                playerKillScrollKey,
                PersistentDataType.BYTE
        );
    }

    public boolean isMobKillScroll(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(
                mobKillScrollKey,
                PersistentDataType.BYTE
        );
    }
}
package com.orbismc.itemloretowny.tracker;

import com.orbismc.itemloretowny.util.LoreUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class KillTracker {
    private final JavaPlugin plugin;
    private final NamespacedKey playerKillsKey;
    private final NamespacedKey mobKillsKey;

    public KillTracker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerKillsKey = new NamespacedKey(plugin, "player_kills");
        this.mobKillsKey = new NamespacedKey(plugin, "mob_kills");
    }

    public void incrementPlayerKills(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        int currentKills = container.getOrDefault(playerKillsKey, PersistentDataType.INTEGER, 0);
        container.set(playerKillsKey, PersistentDataType.INTEGER, currentKills + 1);

        item.setItemMeta(meta);
        updateKillLore(item);
    }

    public void incrementMobKills(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        int currentKills = container.getOrDefault(mobKillsKey, PersistentDataType.INTEGER, 0);
        container.set(mobKillsKey, PersistentDataType.INTEGER, currentKills + 1);

        item.setItemMeta(meta);
        updateKillLore(item);
    }

    public void updateKillLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        int playerKills = container.getOrDefault(playerKillsKey, PersistentDataType.INTEGER, 0);
        int mobKills = container.getOrDefault(mobKillsKey, PersistentDataType.INTEGER, 0);

        // Use LoreUtil to update the lore with kill counts
        LoreUtil.updateKillsLore(item, playerKills, mobKills);
    }

    public boolean hasKillTracker(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(playerKillsKey, PersistentDataType.INTEGER) ||
                container.has(mobKillsKey, PersistentDataType.INTEGER);
    }

    public boolean addTrackerToItem(ItemStack item, TrackerType type) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        switch (type) {
            case PLAYER:
                if (!container.has(playerKillsKey, PersistentDataType.INTEGER)) {
                    container.set(playerKillsKey, PersistentDataType.INTEGER, 0);
                    item.setItemMeta(meta);
                    updateKillLore(item);
                    return true;
                }
                break;
            case MOB:
                if (!container.has(mobKillsKey, PersistentDataType.INTEGER)) {
                    container.set(mobKillsKey, PersistentDataType.INTEGER, 0);
                    item.setItemMeta(meta);
                    updateKillLore(item);
                    return true;
                }
                break;
        }

        return false;
    }

    public enum TrackerType {
        PLAYER, MOB
    }
}
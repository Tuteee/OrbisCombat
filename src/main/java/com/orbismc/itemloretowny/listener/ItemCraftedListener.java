package com.orbismc.itemloretowny.listener;

import com.orbismc.itemloretowny.TownyItemLore;
import com.orbismc.itemloretowny.manager.ItemLoreManager;
import com.orbismc.itemloretowny.util.MaterialUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class ItemCraftedListener implements Listener {
    private final TownyItemLore plugin;

    public ItemCraftedListener(TownyItemLore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result != null) {
            Player player = (Player) event.getWhoClicked();

            // Check if the crafted item is armor, tool, crossbow, bow, or shield
            if (MaterialUtil.targetMaterials.contains(result.getType())) {
                ItemLoreManager.applyLore(result, player);
                // Removed anvil sound effect
            }
        }
    }
}
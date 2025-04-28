package com.orbismc.itemloretowny.manager;

import com.orbismc.itemloretowny.util.MaterialUtil;
import com.orbismc.itemloretowny.util.LoreUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemLoreManager {
    public static void applyLore(ItemStack itemStack, Player player) {
        Material itemType = itemStack.getType();
        if (MaterialUtil.targetMaterials.contains(itemType)) {
            ItemMeta meta = itemStack.getItemMeta();
            List<Component> lore = LoreUtil.generateLore(player);
            meta.lore(lore);
            itemStack.setItemMeta(meta);
        }
    }
}
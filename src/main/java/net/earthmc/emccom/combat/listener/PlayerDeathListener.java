package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import net.earthmc.emccom.EMCCOM;
import net.earthmc.emccom.combat.CombatHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;

public class PlayerDeathListener implements Listener {

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Handle arena keep inventory
        handleArenaKeepInventory(event, victim);

        // Handle player head drops
        if (killer != null) {
            handlePlayerHeadDrop(event, victim, killer);
        }
    }

    private void handleArenaKeepInventory(PlayerDeathEvent event, Player victim) {
        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(victim.getLocation());
        
        // Check if player is in an arena
        if (townBlock != null && townBlock.getType() == TownBlockType.ARENA && townBlock.hasTown()) {
            boolean requiresCombatTag = EMCCOM.getInstance().getConfig().getBoolean("arena_keepinv_requires_combat_tag", true);
            
            if (requiresCombatTag) {
                // Only enable keep inventory if player was combat tagged
                if (CombatHandler.isTagged(victim)) {
                    event.setKeepInventory(true);
                    event.getDrops().clear();
                    event.setKeepLevel(true);
                    event.setDroppedExp(0);
                    
                    victim.sendMessage(Component.text("You kept your items because you died in combat within an arena!", NamedTextColor.GREEN));
                }
            } else {
                // Always enable keep inventory in arena regardless of combat tag
                event.setKeepInventory(true);
                event.getDrops().clear();
                event.setKeepLevel(true);
                event.setDroppedExp(0);
                
                victim.sendMessage(Component.text("You kept your items because you died in an arena!", NamedTextColor.GREEN));
            }
        }
    }

    private void handlePlayerHeadDrop(PlayerDeathEvent event, Player victim, Player killer) {
        double dropChance = EMCCOM.getInstance().getConfig().getDouble("player_head_drop_chance", 0.1);
        
        // Check if we should drop a head based on the configured chance
        if (random.nextDouble() < dropChance) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(victim);
                skullMeta.displayName(Component.text(victim.getName() + "'s Head", NamedTextColor.YELLOW));
                playerHead.setItemMeta(skullMeta);
                
                // Add the head to the drops
                event.getDrops().add(playerHead);
                
                // Notify the killer
                killer.sendMessage(Component.text("You obtained " + victim.getName() + "'s head!", NamedTextColor.GOLD));
            }
        }
    }
}
package net.earthmc.emccom.stamina.listener;

import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.stamina.StaminaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class StaminaListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (CombatHandler.isTagged(player)) {
            StaminaManager.initializePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        StaminaManager.removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player jumped (Y velocity increase)
        if (event.getTo() != null && event.getFrom() != null) {
            double yDiff = event.getTo().getY() - event.getFrom().getY();
            if (yDiff > 0.41 && yDiff < 0.43 && player.isOnGround() == false) {
                // Player jumped
                if (!StaminaManager.canPerformAction(player, StaminaManager.StaminaAction.JUMP)) {
                    // Cancel the jump by setting them back down
                    event.setCancelled(true);
                    player.sendMessage(Component.text("You're too exhausted to jump!", NamedTextColor.RED));
                    return;
                }
                
                StaminaManager.consumeStamina(player, StaminaManager.StaminaAction.JUMP);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        
        if (event.isSprinting() && CombatHandler.isTagged(player)) {
            // Check if player has enough stamina to start sprinting
            if (StaminaManager.getStamina(player) < 10.0) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You're too exhausted to sprint!", NamedTextColor.RED));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!CombatHandler.isTagged(attacker)) {
            return;
        }

        // Check if player can attack
        if (!StaminaManager.canPerformAction(attacker, StaminaManager.StaminaAction.ATTACK)) {
            event.setCancelled(true);
            attacker.sendMessage(Component.text("You're too exhausted to attack!", NamedTextColor.RED));
            return;
        }

        // Consume stamina for attacking
        StaminaManager.consumeStamina(attacker, StaminaManager.StaminaAction.ATTACK);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTakeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!CombatHandler.isTagged(victim)) {
            return;
        }

        // Record that this player took damage (prevents stamina regen)
        StaminaManager.recordDamage(victim);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!CombatHandler.isTagged(player)) {
            return;
        }

        // Check for shield blocking
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.SHIELD) {
            if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || 
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                
                // Check if player can block
                if (!StaminaManager.canPerformAction(player, StaminaManager.StaminaAction.BLOCK)) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("You're too exhausted to block!", NamedTextColor.RED));
                    return;
                }
                
                // Consume stamina for blocking
                StaminaManager.consumeStamina(player, StaminaManager.StaminaAction.BLOCK);
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        
        if (!CombatHandler.isTagged(player)) {
            return;
        }

        // Check if switching to a shield while exhausted
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null && newItem.getType() == Material.SHIELD) {
            if (StaminaManager.getStamina(player) < 5.0) {
                player.sendMessage(Component.text("You're too exhausted to ready your shield!", NamedTextColor.RED));
            }
        }
    }

    // Initialize stamina when combat starts (but don't reset existing stamina)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatStart(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (event.getEntity() instanceof Player victim && event.getDamager() instanceof Player attacker) {
            // Initialize stamina for both players when combat starts
            // This will only create new stamina data if they don't already have it
            StaminaManager.initializePlayer(victim);
            StaminaManager.initializePlayer(attacker);
        }
    }
}
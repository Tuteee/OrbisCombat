package net.earthmc.emccom.armor.listener;

import net.earthmc.emccom.EMCCOM;
import net.earthmc.emccom.armor.ArmorManager;
import net.earthmc.emccom.combat.CombatHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class ArmorListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Delay armor update slightly to ensure player is fully loaded
        EMCCOM.getInstance().getServer().getScheduler().runTaskLater(EMCCOM.getInstance(), () -> {
            ArmorManager.updatePlayerArmor(player);
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ArmorManager.removePlayer(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Check if armor was changed
        if (event.getSlotType() == InventoryType.SlotType.ARMOR || 
            isArmorItem(event.getCurrentItem()) || 
            isArmorItem(event.getCursor())) {
            
            // Update armor stats after the inventory change with a small delay
            EMCCOM.getInstance().getServer().getScheduler().runTaskLater(EMCCOM.getInstance(), () -> {
                ArmorManager.updatePlayerArmor(player);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack brokenItem = event.getBrokenItem();
        
        if (isArmorItem(brokenItem)) {
            // Delay armor update to ensure the broken item is removed
            EMCCOM.getInstance().getServer().getScheduler().runTaskLater(EMCCOM.getInstance(), () -> {
                ArmorManager.updatePlayerArmor(player);
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        // Check if armor system is enabled
        if (!EMCCOM.getInstance().getConfig().getBoolean("armor.enabled", true)) {
            return;
        }

        // Handle dodge chance for light armor
        if (EMCCOM.getInstance().getConfig().getBoolean("armor.dodge_enabled", true) && 
            ArmorManager.attemptDodge(victim)) {
            event.setCancelled(true);
            victim.sendMessage(Component.text("You dodged the attack!", NamedTextColor.GREEN));
            
            // Notify attacker
            Player attacker = getAttacker(event);
            if (attacker != null) {
                attacker.sendMessage(Component.text(victim.getName() + " dodged your attack!", NamedTextColor.YELLOW));
            }
            return;
        }

        // Apply armor protection
        double originalDamage = event.getDamage();
        double reducedDamage = ArmorManager.calculateDamageReduction(victim, originalDamage);
        
        // Handle armor piercing for crossbows
        if (isCrossbowAttack(event)) {
            double armorPiercing = EMCCOM.getInstance().getConfig().getDouble("armor.crossbow_armor_piercing", 0.3);
            // Crossbows bypass configured percentage of armor protection
            double bypassedDamage = originalDamage * armorPiercing;
            double protectedDamage = ArmorManager.calculateDamageReduction(victim, originalDamage * (1.0 - armorPiercing));
            reducedDamage = bypassedDamage + protectedDamage;
        }

        event.setDamage(reducedDamage);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if swimming restrictions are enabled
        if (!EMCCOM.getInstance().getConfig().getBoolean("armor.swimming_restrictions", true)) {
            return;
        }
        
        // Only check if player is actually moving and is in water
        if (event.getFrom().equals(event.getTo()) || !player.isInWater()) {
            return;
        }
        
        // Check for swimming restrictions in heavy armor
        if (player.isSwimming() && !ArmorManager.canPlayerSwim(player)) {
            // Cancel movement and notify player
            event.setCancelled(true);
            
            // Send message with cooldown to prevent spam
            if (System.currentTimeMillis() % 1000 < 50) { // Only send message roughly once per second
                player.sendMessage(Component.text("You cannot swim effectively in heavy armor!", NamedTextColor.RED));
            }
            
            // Apply drowning-like effects
            if (player.getRemainingAir() > 0) {
                player.setRemainingAir(Math.max(0, player.getRemainingAir() - 10));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if hunger penalties are enabled
        if (!EMCCOM.getInstance().getConfig().getBoolean("armor.hunger_penalties", true)) {
            return;
        }

        // Only apply penalties when food level is decreasing
        if (event.getFoodLevel() >= player.getFoodLevel()) {
            return;
        }

        // Increase hunger loss when in combat with armor that has hunger penalties
        if (CombatHandler.isTagged(player)) {
            double hungerMultiplier = ArmorManager.getHungerMultiplier(player);
            
            // Apply additional hunger loss
            if (hungerMultiplier > 0) {
                int currentLoss = player.getFoodLevel() - event.getFoodLevel();
                int additionalLoss = (int) Math.ceil(currentLoss * hungerMultiplier);
                int newFoodLevel = Math.max(0, event.getFoodLevel() - additionalLoss);
                event.setFoodLevel(newFoodLevel);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        
        if (event.isSprinting() && ArmorManager.isWearingHeavyArmor(player)) {
            // Heavy armor users drain hunger faster when sprinting
            if (player.getFoodLevel() <= 6) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You're too exhausted to sprint in heavy armor!", NamedTextColor.RED));
            }
        }
    }

    // Add this new event handler for when players change armor manually
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        // Update armor if player interacted with armor stand
        EMCCOM.getInstance().getServer().getScheduler().runTaskLater(EMCCOM.getInstance(), () -> {
            ArmorManager.updatePlayerArmor(event.getPlayer());
        }, 1L);
    }

    // Add handler for when players equip armor by right-clicking
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item != null && isArmorItem(item) && 
            (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || 
             event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
            
            // Delay update to allow the equip to process
            EMCCOM.getInstance().getServer().getScheduler().runTaskLater(EMCCOM.getInstance(), () -> {
                ArmorManager.updatePlayerArmor(player);
            }, 2L);
        }
    }

    private boolean isArmorItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        
        String materialName = item.getType().name();
        return materialName.endsWith("_HELMET") || 
               materialName.endsWith("_CHESTPLATE") || 
               materialName.endsWith("_LEGGINGS") || 
               materialName.endsWith("_BOOTS");
    }

    private Player getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        } else if (event.getDamager() instanceof Projectile projectile && 
                   projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }

    private boolean isCrossbowAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                ItemStack mainHand = shooter.getInventory().getItemInMainHand();
                return mainHand.getType() == Material.CROSSBOW;
            }
        }
        return false;
    }
}
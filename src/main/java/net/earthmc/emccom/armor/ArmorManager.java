package net.earthmc.emccom.armor;

import net.earthmc.emccom.EMCCOM;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorManager {
    private static final Map<UUID, ArmorStats> playerArmorStats = new ConcurrentHashMap<>();
    private static final NamespacedKey SPEED_MODIFIER_KEY = new NamespacedKey(EMCCOM.getInstance(), "armor_speed");
    private static final NamespacedKey JUMP_MODIFIER_KEY = new NamespacedKey(EMCCOM.getInstance(), "armor_jump");

    public static void updatePlayerArmor(Player player) {
        // Schedule on main thread to ensure proper timing
        EMCCOM.getInstance().getServer().getScheduler().runTask(EMCCOM.getInstance(), () -> {
            ArmorStats stats = new ArmorStats(player);
            playerArmorStats.put(player.getUniqueId(), stats);
            
            applyArmorEffects(player, stats);
        });
    }

    public static void removePlayer(Player player) {
        playerArmorStats.remove(player.getUniqueId());
        removeArmorEffects(player);
    }

    public static ArmorStats getArmorStats(Player player) {
        return playerArmorStats.get(player.getUniqueId());
    }

    private static void applyArmorEffects(Player player, ArmorStats stats) {
        // Apply movement speed modifier
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // Remove existing modifier first - create a list to avoid concurrent modification
            List<AttributeModifier> speedModifiersToRemove = speedAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(SPEED_MODIFIER_KEY))
                .toList();
            speedModifiersToRemove.forEach(speedAttribute::removeModifier);

            // Apply new speed modifier if different from default
            double speedMultiplier = stats.getSpeedMultiplier();
            if (speedMultiplier != 1.0) {
                // Use MULTIPLY_SCALAR_1 which multiplies by (1 + value)
                // So if we want 70% speed, we need -0.3 as the modifier value
                double modifierValue = speedMultiplier - 1.0;
                AttributeModifier modifier = new AttributeModifier(
                    SPEED_MODIFIER_KEY,
                    modifierValue,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                speedAttribute.addModifier(modifier);
                EMCCOM.getInstance().getLogger().info("DEBUG: Applied speed modifier " + modifierValue + " (target: " + speedMultiplier + ")");
            }
        }

        // Apply jump strength modifier
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            // Remove existing modifier first
            List<AttributeModifier> jumpModifiersToRemove = jumpAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(JUMP_MODIFIER_KEY))
                .toList();
            jumpModifiersToRemove.forEach(jumpAttribute::removeModifier);

            // Apply new jump modifier if different from default
            double jumpMultiplier = stats.getJumpMultiplier();
            if (jumpMultiplier != 1.0) {
                double modifierValue = jumpMultiplier - 1.0;
                AttributeModifier modifier = new AttributeModifier(
                    JUMP_MODIFIER_KEY,
                    modifierValue,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                jumpAttribute.addModifier(modifier);
                EMCCOM.getInstance().getLogger().info("DEBUG: Applied jump modifier " + modifierValue + " (target: " + jumpMultiplier + ")");
            }
        }
    }

    private static void removeArmorEffects(Player player) {
        // Remove speed modifier
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            List<AttributeModifier> speedModifiersToRemove = speedAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(SPEED_MODIFIER_KEY))
                .toList();
            speedModifiersToRemove.forEach(speedAttribute::removeModifier);
        }

        // Remove jump modifier
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            List<AttributeModifier> jumpModifiersToRemove = jumpAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(JUMP_MODIFIER_KEY))
                .toList();
            jumpModifiersToRemove.forEach(jumpAttribute::removeModifier);
        }
    }

    public static double calculateDamageReduction(Player player, double damage) {
        ArmorStats stats = getArmorStats(player);
        if (stats == null) {
            return damage;
        }

        // Apply protection reduction
        double protectedDamage = damage * (1.0 - stats.getTotalProtection());
        
        return Math.max(0.0, protectedDamage);
    }

    public static boolean attemptDodge(Player player) {
        ArmorStats stats = getArmorStats(player);
        if (stats == null) {
            return false;
        }

        return Math.random() < stats.getDodgeChance();
    }

    public static void refreshAllPlayerArmor() {
        // Run on main thread
        EMCCOM.getInstance().getServer().getScheduler().runTask(EMCCOM.getInstance(), () -> {
            for (UUID uuid : playerArmorStats.keySet()) {
                Player player = EMCCOM.getInstance().getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    updatePlayerArmor(player);
                }
            }
        });
    }

    public static boolean canPlayerSwim(Player player) {
        ArmorStats stats = getArmorStats(player);
        return stats == null || stats.canSwimEffectively();
    }

    public static double getHungerMultiplier(Player player) {
        ArmorStats stats = getArmorStats(player);
        return stats != null ? stats.getHungerMultiplier() : 0.0;
    }

    public static double getStaminaMultiplier(Player player) {
        ArmorStats stats = getArmorStats(player);
        return stats != null ? stats.getStaminaMultiplier() : 1.0;
    }

    public static boolean isWearingHeavyArmor(Player player) {
        ArmorStats stats = getArmorStats(player);
        return stats != null && stats.isWearingHeavyArmor();
    }
}
package net.earthmc.emccom.armor;

import net.earthmc.emccom.EMCCOM;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorManager {
    private static final Map<UUID, ArmorStats> playerArmorStats = new ConcurrentHashMap<>();
    private static final NamespacedKey SPEED_MODIFIER_KEY = new NamespacedKey(EMCCOM.getInstance(), "armor_speed");
    private static final NamespacedKey JUMP_MODIFIER_KEY = new NamespacedKey(EMCCOM.getInstance(), "armor_jump");

    public static void updatePlayerArmor(Player player) {
        ArmorStats stats = new ArmorStats(player);
        playerArmorStats.put(player.getUniqueId(), stats);
        
        applyArmorEffects(player, stats);
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
            // Remove existing modifier
            speedAttribute.getModifiers().forEach(modifier -> {
                if (modifier.getKey().equals(SPEED_MODIFIER_KEY)) {
                    speedAttribute.removeModifier(modifier);
                }
            });

            // Apply new speed modifier (multiplicative)
            double speedModifier = stats.getSpeedMultiplier() - 1.0; // Convert to modifier value
            if (speedModifier != 0.0) {
                AttributeModifier modifier = new AttributeModifier(
                    SPEED_MODIFIER_KEY,
                    speedModifier,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                speedAttribute.addModifier(modifier);
            }
        }

        // Apply jump strength modifier
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            // Remove existing modifier
            jumpAttribute.getModifiers().forEach(modifier -> {
                if (modifier.getKey().equals(JUMP_MODIFIER_KEY)) {
                    jumpAttribute.removeModifier(modifier);
                }
            });

            // Apply new jump modifier
            double jumpModifier = stats.getJumpMultiplier() - 1.0;
            if (jumpModifier != 0.0) {
                AttributeModifier modifier = new AttributeModifier(
                    JUMP_MODIFIER_KEY,
                    jumpModifier,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
                );
                jumpAttribute.addModifier(modifier);
            }
        }
    }

    private static void removeArmorEffects(Player player) {
        // Remove speed modifier
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.getModifiers().forEach(modifier -> {
                if (modifier.getKey().equals(SPEED_MODIFIER_KEY)) {
                    speedAttribute.removeModifier(modifier);
                }
            });
        }

        // Remove jump modifier
        AttributeInstance jumpAttribute = player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.getModifiers().forEach(modifier -> {
                if (modifier.getKey().equals(JUMP_MODIFIER_KEY)) {
                    jumpAttribute.removeModifier(modifier);
                }
            });
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
        playerArmorStats.values().forEach(ArmorStats::refresh);
        playerArmorStats.forEach((uuid, stats) -> {
            Player player = EMCCOM.getInstance().getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                applyArmorEffects(player, stats);
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
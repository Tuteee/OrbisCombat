package net.earthmc.emccom.armor;

import net.earthmc.emccom.EMCCOM;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import com.nexomc.nexo.api.NexoItems;

public class ArmorStats {
    private final Player player;
    private double totalProtection;
    private double speedMultiplier;
    private double jumpMultiplier;
    private double dodgeChance;
    private double hungerMultiplier;
    private double staminaMultiplier;
    private int armorPieces;
    private double averageDurability;

    public ArmorStats(Player player) {
        this.player = player;
        calculateStats();
    }

    private void calculateStats() {
        // Reset stats
        totalProtection = 0.0;
        speedMultiplier = 1.0;
        jumpMultiplier = 1.0;
        dodgeChance = 0.0;
        hungerMultiplier = 0.0;
        staminaMultiplier = 1.0;
        armorPieces = 0;
        averageDurability = 1.0;

        ItemStack[] armorContents = player.getInventory().getArmorContents();
        double totalDurability = 0.0;
        int durabilityCount = 0;

        for (ItemStack armor : armorContents) {
            if (armor == null || armor.getType().isAir()) {
                continue;
            }

            ArmorType armorType = getArmorType(armor);
            if (armorType == null) {
                continue;
            }

            armorPieces++;

            // Calculate durability percentage for this piece
            double durabilityPercent = calculateDurabilityPercent(armor);
            totalDurability += durabilityPercent;
            durabilityCount++;

            // Apply damage penalties if durability < 50% and durability penalties are enabled
            double damageMultiplier = 1.0;
            if (EMCCOM.getInstance().getConfig().getBoolean("armor.durability_penalties", true) && 
                durabilityPercent < 0.5) {
                damageMultiplier = 0.7;
            }

            // Accumulate stats based on armor type
            totalProtection += armorType.getProtectionMultiplier() * damageMultiplier;
            
            double speedPenalty = (EMCCOM.getInstance().getConfig().getBoolean("armor.durability_penalties", true) && 
                                  durabilityPercent < 0.5) ? 0.8 : 1.0;
            double jumpPenalty = (EMCCOM.getInstance().getConfig().getBoolean("armor.durability_penalties", true) && 
                                 durabilityPercent < 0.5) ? 0.8 : 1.0;
            double hungerPenalty = (EMCCOM.getInstance().getConfig().getBoolean("armor.durability_penalties", true) && 
                                   durabilityPercent < 0.5) ? 1.5 : 1.0;
            
            // Fix: Use multiplication properly for speed/jump
            speedMultiplier *= armorType.getSpeedMultiplier() * speedPenalty;
            jumpMultiplier *= armorType.getJumpMultiplier() * jumpPenalty;
            dodgeChance += armorType.getDodgeChance() * damageMultiplier;
            hungerMultiplier += armorType.getHungerMultiplier() * hungerPenalty;
            staminaMultiplier *= armorType.getStaminaMultiplier();
        }

        // Calculate average durability
        if (durabilityCount > 0) {
            averageDurability = totalDurability / durabilityCount;
        }

        // Apply minimum limits to prevent extreme values
        speedMultiplier = Math.max(0.1, speedMultiplier);
        jumpMultiplier = Math.max(0.1, jumpMultiplier);

        // Normalize stats based on number of armor pieces
        if (armorPieces > 0) {
            double maxProtection = EMCCOM.getInstance().getConfig().getDouble("armor.max_protection", 0.8);
            double maxDodgeChance = EMCCOM.getInstance().getConfig().getDouble("armor.max_dodge_chance", 0.25);
            
            totalProtection = Math.min(maxProtection, totalProtection);
            dodgeChance = Math.min(maxDodgeChance, dodgeChance);
        }
    }

    private ArmorType getArmorType(ItemStack armor) {
        // Check if Nexo is enabled and if it's a Nexo item first
        if (EMCCOM.getInstance().getServer().getPluginManager().isPluginEnabled("Nexo")) {
            try {
                String nexoId = NexoItems.idFromItem(armor);
                if (nexoId != null) {
                    ArmorType nexoType = ArmorType.fromNexoId(nexoId);
                    if (nexoType != null) {
                        return nexoType;
                    }
                }
            } catch (Exception e) {
                // Fall through to vanilla detection
            }
        }

        // Fall back to vanilla armor detection
        return ArmorType.fromVanillaArmor(armor.getType());
    }

    private double calculateDurabilityPercent(ItemStack armor) {
        if (!(armor.getItemMeta() instanceof Damageable damageable)) {
            return 1.0;
        }

        int maxDurability = armor.getType().getMaxDurability();
        if (maxDurability == 0) {
            return 1.0;
        }

        int damage = damageable.getDamage();
        return Math.max(0.0, (double) (maxDurability - damage) / maxDurability);
    }

    public void refresh() {
        calculateStats();
    }

    // Getters
    public double getTotalProtection() {
        return totalProtection;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getJumpMultiplier() {
        return jumpMultiplier;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public double getHungerMultiplier() {
        return hungerMultiplier;
    }

    public double getStaminaMultiplier() {
        return staminaMultiplier;
    }

    public int getArmorPieces() {
        return armorPieces;
    }

    public double getAverageDurability() {
        return averageDurability;
    }

    public boolean isWearingHeavyArmor() {
        return armorPieces >= 2 && speedMultiplier < 0.8;
    }

    public boolean canSwimEffectively() {
        // Swimming becomes very difficult in heavy armor
        return speedMultiplier > 0.6;
    }

    public Player getPlayer() {
        return player;
    }
}
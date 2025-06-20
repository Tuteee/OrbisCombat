package net.earthmc.emccom.armor;

import net.earthmc.emccom.EMCCOM;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public enum ArmorType {
    LIGHT("light"),
    MEDIUM("medium"),
    HEAVY("heavy"),
    WIZARD("wizard");

    private final String configKey;

    ArmorType(String configKey) {
        this.configKey = configKey;
    }

    public String getName() {
        return configKey;
    }

    public double getProtectionMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        return config.getDouble("armor." + configKey + ".protection_multiplier", 0.1);
    }

    public double getSpeedMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        return config.getDouble("armor." + configKey + ".speed_multiplier", 1.0);
    }

    public double getJumpMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        return config.getDouble("armor." + configKey + ".jump_multiplier", 1.0);
    }

    public double getDodgeChance() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        if (!config.getBoolean("armor.dodge_enabled", true)) {
            return 0.0;
        }
        return config.getDouble("armor." + configKey + ".dodge_chance", 0.0);
    }

    public double getHungerMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        if (!config.getBoolean("armor.hunger_penalties", true)) {
            return 0.0;
        }
        return config.getDouble("armor." + configKey + ".hunger_multiplier", 0.0);
    }

    public double getStaminaMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        return config.getDouble("armor." + configKey + ".stamina_multiplier", 1.0);
    }

    public static ArmorType fromNexoId(String nexoId) {
        if (nexoId == null) return null;
        
        String lowerCaseId = nexoId.toLowerCase();
        
        if (lowerCaseId.startsWith("light_")) {
            return LIGHT;
        } else if (lowerCaseId.startsWith("medium_")) {
            return MEDIUM;
        } else if (lowerCaseId.startsWith("heavy_")) {
            return HEAVY;
        } else if (lowerCaseId.startsWith("wizard_")) {
            return WIZARD;
        }
        
        return null;
    }

    public static ArmorType fromVanillaArmor(Material material) {
        return switch (material) {
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS,
                 CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS -> LIGHT;
            case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS -> MEDIUM;
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS,
                 NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> HEAVY;
            default -> null;
        };
    }

    public boolean isArmorPiece(Material material) {
        return material.name().endsWith("_HELMET") || 
               material.name().endsWith("_CHESTPLATE") || 
               material.name().endsWith("_LEGGINGS") || 
               material.name().endsWith("_BOOTS");
    }
}
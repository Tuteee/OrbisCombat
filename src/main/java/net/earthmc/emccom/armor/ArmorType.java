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
        String configPath = "armor." + configKey + ".protection_multiplier";
        
        // Use get() and manually convert to double to avoid type conversion issues
        Object value = config.get(configPath);
        double result;
        
        if (value instanceof Number) {
            result = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 0.1; // Default fallback
            }
        } else {
            result = 0.1; // Default fallback
        }
        
        return result;
    }

    public double getSpeedMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        String configPath = "armor." + configKey + ".speed_multiplier";
        
        // Use get() and manually convert to double to avoid type conversion issues
        Object value = config.get(configPath);
        double result;
        
        if (value instanceof Number) {
            result = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 1.0; // Default fallback
            }
        } else {
            result = 1.0; // Default fallback
        }
        
        return result;
    }

    public double getJumpMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        String configPath = "armor." + configKey + ".jump_multiplier";
        
        // Use get() and manually convert to double to avoid type conversion issues
        Object value = config.get(configPath);
        double result;
        
        if (value instanceof Number) {
            result = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 1.0; // Default fallback
            }
        } else {
            result = 1.0; // Default fallback
        }
        
        return result;
    }

    public double getDodgeChance() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        if (!config.getBoolean("armor.dodge_enabled", true)) {
            return 0.0;
        }
        
        String configPath = "armor." + configKey + ".dodge_chance";
        Object value = config.get(configPath);
        double result;
        
        if (value instanceof Number) {
            result = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 0.0;
            }
        } else {
            result = 0.0;
        }
        
        return result;
    }

    public double getHungerMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        if (!config.getBoolean("armor.hunger_penalties", true)) {
            return 0.0;
        }
        
        String configPath = "armor." + configKey + ".hunger_multiplier";
        Object value = config.get(configPath);
        double result;
        
        if (value instanceof Number) {
            result = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 0.0;
            }
        } else {
            result = 0.0;
        }
        
        return result;
    }

    public double getStaminaMultiplier() {
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        String configPath = "armor." + configKey + ".stamina_multiplier";
        Object value = config.get(configPath);
        double result;
        
        if (value instanceof Number) {
            result = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                result = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                result = 1.0;
            }
        } else {
            result = 1.0;
        }
        
        return result;
    }

    public static ArmorType fromNexoId(String nexoId) {
        if (nexoId == null) {
            return null;
        }
        
        String lowerCaseId = nexoId.toLowerCase();
        
        // Check for exact matches with your armor naming pattern
        if (lowerCaseId.startsWith("light_")) {
            return LIGHT;
        } else if (lowerCaseId.startsWith("medium_")) {
            return MEDIUM;
        } else if (lowerCaseId.startsWith("heavy_")) {
            return HEAVY;
        } else if (lowerCaseId.startsWith("wizard_")) {
            return WIZARD;
        }
        
        // Also check if the ID contains the armor type anywhere
        if (lowerCaseId.contains("light")) {
            return LIGHT;
        } else if (lowerCaseId.contains("medium")) {
            return MEDIUM;
        } else if (lowerCaseId.contains("heavy")) {
            return HEAVY;
        } else if (lowerCaseId.contains("wizard")) {
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
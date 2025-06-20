package net.earthmc.emccom.config;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static void init(FileConfiguration config) {
        config.addDefault("ender_pearl_cooldown_ticks", 240);
        config.addDefault("new_player_protection_hours", 24);
        config.addDefault("player_head_drop_chance", 0.1);
        config.addDefault("arena_keepinv_requires_combat_tag", true);
        
        // Armor system settings
        config.addDefault("armor.enabled", true);
        config.addDefault("armor.dodge_enabled", true);
        config.addDefault("armor.swimming_restrictions", true);
        config.addDefault("armor.hunger_penalties", true);
        config.addDefault("armor.crossbow_armor_piercing", 0.3);
        config.addDefault("armor.durability_penalties", true);
        config.addDefault("armor.max_protection", 0.8);
        config.addDefault("armor.max_dodge_chance", 0.25);

        // Light armor stats
        config.addDefault("armor.light.protection_multiplier", 0.1);
        config.addDefault("armor.light.speed_multiplier", 0.95);
        config.addDefault("armor.light.jump_multiplier", 1.2);
        config.addDefault("armor.light.dodge_chance", 0.15);
        config.addDefault("armor.light.hunger_multiplier", 0.1);
        config.addDefault("armor.light.stamina_multiplier", 1.0);

        // Medium armor stats
        config.addDefault("armor.medium.protection_multiplier", 0.3);
        config.addDefault("armor.medium.speed_multiplier", 0.85);
        config.addDefault("armor.medium.jump_multiplier", 1.0);
        config.addDefault("armor.medium.dodge_chance", 0.08);
        config.addDefault("armor.medium.hunger_multiplier", 0.05);
        config.addDefault("armor.medium.stamina_multiplier", 0.8);

        // Heavy armor stats
        config.addDefault("armor.heavy.protection_multiplier", 0.6);
        config.addDefault("armor.heavy.speed_multiplier", 0.7);
        config.addDefault("armor.heavy.jump_multiplier", 0.7);
        config.addDefault("armor.heavy.dodge_chance", 0.0);
        config.addDefault("armor.heavy.hunger_multiplier", 0.0);
        config.addDefault("armor.heavy.stamina_multiplier", 0.5);

        // Wizard armor stats
        config.addDefault("armor.wizard.protection_multiplier", 0.05);
        config.addDefault("armor.wizard.speed_multiplier", 1.0);
        config.addDefault("armor.wizard.jump_multiplier", 1.1);
        config.addDefault("armor.wizard.dodge_chance", 0.20);
        config.addDefault("armor.wizard.hunger_multiplier", 0.15);
        config.addDefault("armor.wizard.stamina_multiplier", 1.2);

        config.options().copyDefaults(true);
    }
}
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
        config.addDefault("armor.dodge_enabled", false); // Changed to match your config
        config.addDefault("armor.swimming_restrictions", true);
        config.addDefault("armor.hunger_penalties", true);
        config.addDefault("armor.crossbow_armor_piercing", 0.3);
        config.addDefault("armor.durability_penalties", true);
        config.addDefault("armor.max_protection", 0.8);
        config.addDefault("armor.max_dodge_chance", 0.25);

        // Light armor stats - updated to match your config
        config.addDefault("armor.light.protection_multiplier", 0.03);
        config.addDefault("armor.light.speed_multiplier", 0.95);
        config.addDefault("armor.light.jump_multiplier", 1.0);
        config.addDefault("armor.light.dodge_chance", 0.6);
        config.addDefault("armor.light.hunger_multiplier", 0.1);
        config.addDefault("armor.light.stamina_multiplier", 1.0);

        // Medium armor stats - updated to match your config
        config.addDefault("armor.medium.protection_multiplier", 0.1);
        config.addDefault("armor.medium.speed_multiplier", 0.85);
        config.addDefault("armor.medium.jump_multiplier", 0.99);
        config.addDefault("armor.medium.dodge_chance", 0.03);
        config.addDefault("armor.medium.hunger_multiplier", 0.05);
        config.addDefault("armor.medium.stamina_multiplier", 0.8);

        // Heavy armor stats - updated to match your config
        config.addDefault("armor.heavy.protection_multiplier", 0.15);
        config.addDefault("armor.heavy.speed_multiplier", 0.8);
        config.addDefault("armor.heavy.jump_multiplier", 0.98);
        config.addDefault("armor.heavy.dodge_chance", 0.0);
        config.addDefault("armor.heavy.hunger_multiplier", 0.0);
        config.addDefault("armor.heavy.stamina_multiplier", 0.7);

        // Wizard armor stats - updated to match your config
        config.addDefault("armor.wizard.protection_multiplier", 0.0);
        config.addDefault("armor.wizard.speed_multiplier", 1.0);
        config.addDefault("armor.wizard.jump_multiplier", 1.0);
        config.addDefault("armor.wizard.dodge_chance", 0.2);
        config.addDefault("armor.wizard.hunger_multiplier", 0.15);
        config.addDefault("armor.wizard.stamina_multiplier", 1.0);

        // Stamina system settings
        config.addDefault("stamina.enabled", true);
        config.addDefault("stamina.show_action_bar", true);
        config.addDefault("stamina.max_stamina", 100.0);
        config.addDefault("stamina.regen_rate_standing", 0.3); // Per second
        config.addDefault("stamina.regen_rate_walking", 0.2); // Per second
        config.addDefault("stamina.sprint_cost_per_second", 8.0);
        config.addDefault("stamina.jump_cost", 25.0);
        config.addDefault("stamina.attack_cost", 20.0);
        config.addDefault("stamina.block_cost", 10.0);
        config.addDefault("stamina.exhausted_threshold", 20.0);
        config.addDefault("stamina.very_exhausted_threshold", 5.0);
        config.addDefault("stamina.damage_regen_delay_ms", 1000); // No regen for 1 second after taking damage

        config.options().copyDefaults(true);
    }
}
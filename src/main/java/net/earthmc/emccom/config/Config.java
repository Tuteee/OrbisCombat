package net.earthmc.emccom.config;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static void init(FileConfiguration config) {
        config.addDefault("ender_pearl_cooldown_ticks", 240);
        config.addDefault("new_player_protection_hours", 24);
        config.addDefault("player_head_drop_chance", 0.1); // 10% chance by default
        config.addDefault("arena_keepinv_requires_combat_tag", true);

        config.options().copyDefaults(true);
    }
}
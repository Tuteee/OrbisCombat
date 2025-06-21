package net.earthmc.emccom;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import net.earthmc.emccom.armor.listener.ArmorListener;
import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.combat.bossbar.BossBarTask;
import net.earthmc.emccom.combat.listener.CombatListener;
import net.earthmc.emccom.combat.listener.CommandListener;
import net.earthmc.emccom.combat.listener.PlayerItemCooldownListener;
import net.earthmc.emccom.combat.listener.PlayerDeathListener;
import net.earthmc.emccom.commands.ArmorCommand;
import net.earthmc.emccom.commands.CombatCommand;
import net.earthmc.emccom.commands.NationOutlawCommand;
import net.earthmc.emccom.commands.StaminaCommand;
import net.earthmc.emccom.config.Config;
import net.earthmc.emccom.stamina.listener.StaminaListener;
import net.earthmc.emccom.stamina.task.StaminaTask;
import net.earthmc.emccom.util.Translation;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class EMCCOM extends JavaPlugin {
    private static EMCCOM instance;

    public static EMCCOM getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        
        // Check for Nexo dependency
        if (!getServer().getPluginManager().isPluginEnabled("Nexo")) {
            getLogger().warning("Nexo plugin not found! Armor system will only work with vanilla armor.");
        } else {
            getLogger().info("Nexo found! Custom armor support enabled.");
        }
        
        Translation.loadStrings();
        Config.init(getConfig());
        saveConfig();
        setupListeners();
        setupCommands();
        runTasks();
        
        getLogger().info("OrbisCombat enabled with armor and stamina systems!");
    }

    @Override
    public void onDisable() {
        getLogger().info("OrbisCombat disabled!");
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerItemCooldownListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        
        // Register armor system listener only if enabled
        if (getConfig().getBoolean("armor.enabled", true)) {
            getServer().getPluginManager().registerEvents(new ArmorListener(), this);
            getLogger().info("Armor system enabled!");
        } else {
            getLogger().info("Armor system disabled in config.");
        }
        
        // Register stamina system listener only if enabled
        if (getConfig().getBoolean("stamina.enabled", true)) {
            getServer().getPluginManager().registerEvents(new StaminaListener(), this);
            getLogger().info("Stamina system enabled!");
        } else {
            getLogger().info("Stamina system disabled in config.");
        }
    }

    private void setupCommands() {
        // Register main combat commands
        Objects.requireNonNull(getCommand("orbiscombat")).setExecutor(new CombatCommand());
        Objects.requireNonNull(getCommand("combat")).setExecutor(new CombatCommand());

        // Keep old command for backwards compatibility
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatCommand());

        // Register armor command only if armor system is enabled
        if (getConfig().getBoolean("armor.enabled", true)) {
            Objects.requireNonNull(getCommand("armor")).setExecutor(new ArmorCommand());
        }

        // Register stamina command only if stamina system is enabled
        if (getConfig().getBoolean("stamina.enabled", true)) {
            Objects.requireNonNull(getCommand("stamina")).setExecutor(new StaminaCommand());
        }

        // Register the outlaw subcommand for the nation command
        NationOutlawCommand outlawCommand = new NationOutlawCommand();
        TownyCommandAddonAPI.addSubCommand(CommandType.NATION, "outlaw", outlawCommand);
    }

    private void runTasks() {
        // Combat boss bar task - using normal Bukkit scheduler
        new BossBarTask().runTaskTimerAsynchronously(this, 10L, 10L);
        
        // Combat handler task
        CombatHandler.startTask(this);
        
        // Stamina system task (only if enabled) - using normal Bukkit scheduler
        if (getConfig().getBoolean("stamina.enabled", true)) {
            new StaminaTask().runTaskTimer(this, 2L, 2L);
        }
    }
}
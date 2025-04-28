package com.orbismc.itemloretowny;

import com.orbismc.itemloretowny.listener.AnvilListener;
import com.orbismc.itemloretowny.listener.EntityDeathListener;
import com.orbismc.itemloretowny.listener.ItemCraftedListener;
import com.orbismc.itemloretowny.manager.CommandManager;
import com.orbismc.itemloretowny.manager.ScrollManager;
import com.orbismc.itemloretowny.tracker.KillTracker;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TownyItemLore extends JavaPlugin {
    private KillTracker killTracker;
    private ScrollManager scrollManager;

    @Override
    public void onEnable() {
        // Initialize trackers and managers
        killTracker = new KillTracker(this);
        scrollManager = new ScrollManager(this, killTracker);

        // Get the plugin manager
        PluginManager pluginManager = getServer().getPluginManager();

        // Register event listeners
        if (pluginManager != null) {
            // Register the craft listener (removed anvil sound)
            pluginManager.registerEvents(new ItemCraftedListener(this), this);

            // Register kill tracking listeners
            pluginManager.registerEvents(new EntityDeathListener(killTracker), this);

            // Register anvil interaction listener for scrolls
            pluginManager.registerEvents(new AnvilListener(scrollManager, killTracker), this);

            getLogger().info("All listeners have been registered.");
        } else {
            getLogger().warning("Failed to register listeners! Plugin manager is null.");
        }

        // Register commands
        getCommand("townylore").setExecutor(new CommandManager(this));
        getLogger().info("Commands registered.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public KillTracker getKillTracker() {
        return killTracker;
    }

    public ScrollManager getScrollManager() {
        return scrollManager;
    }
}
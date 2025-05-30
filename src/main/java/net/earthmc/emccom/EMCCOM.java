package net.earthmc.emccom;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.combat.bossbar.BossBarTask;
import net.earthmc.emccom.combat.listener.CombatListener;
import net.earthmc.emccom.combat.listener.CommandListener;
import net.earthmc.emccom.combat.listener.PlayerItemCooldownListener;
import net.earthmc.emccom.commands.CombatTagCommand;
import net.earthmc.emccom.commands.NationOutlawCommand;
import net.earthmc.emccom.config.Config;
import net.earthmc.emccom.util.Translation;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class EMCCOM extends JavaPlugin {
    private static EMCCOM instance;

    public static EMCCOM getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Translation.loadStrings();
        Config.init(getConfig());
        saveConfig();
        setupListeners();
        setupCommands();
        runTasks();
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerItemCooldownListener(this), this);
    }

    private void setupCommands() {
        Objects.requireNonNull(getCommand("combattag")).setExecutor(new CombatTagCommand());

        // Register the outlaw subcommand for the nation command
        NationOutlawCommand outlawCommand = new NationOutlawCommand();
        TownyCommandAddonAPI.addSubCommand(CommandType.NATION, "outlaw", outlawCommand);
    }

    private void runTasks() {
        getServer().getAsyncScheduler().runAtFixedRate(this, new BossBarTask(), 500L, 500L, TimeUnit.MILLISECONDS);
        CombatHandler.startTask(this);
    }
}
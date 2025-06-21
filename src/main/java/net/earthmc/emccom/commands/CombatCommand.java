package net.earthmc.emccom.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.emccom.EMCCOM;
import net.earthmc.emccom.armor.ArmorManager;
import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.config.Config;
import net.earthmc.emccom.manager.NewPlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CombatCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> showHelp(sender);
            case "tag" -> parseTagCommand(sender, args);
            case "untag" -> parseUntagCommand(sender, args);
            case "status" -> parseStatusCommand(sender, args);
            case "protection" -> parseProtectionCommand(sender, args);
            case "reload" -> parseReloadCommand(sender, args);
            default -> {
                sender.sendMessage(Component.text("[OrbisCombat]: Incorrect Usage: /" + label + " help", NamedTextColor.RED));
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("[OrbisCombat]: Help Commands", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/" + getMainCommand(sender) + " tag <username> - Tag a player for combat", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + getMainCommand(sender) + " untag <username> - Remove combat tag", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + getMainCommand(sender) + " status [username] - Check combat/protection status", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/" + getMainCommand(sender) + " protection disable - Disable new player protection", NamedTextColor.GRAY));
        
        if (sender.hasPermission("emccom.command.reload")) {
            sender.sendMessage(Component.text("/" + getMainCommand(sender) + " reload - Reload plugin configuration", NamedTextColor.GRAY));
        }
    }

    private String getMainCommand(CommandSender sender) {
        // Default to orbiscombat, but could be customized based on context
        return "orbiscombat";
    }

    private void parseTagCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.combattag")) {
            sender.sendMessage(Component.text("[OrbisCombat]: You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        Player target;

        if (args.length < 2) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Component.text("[OrbisCombat]: Incorrect Usage: /" + getMainCommand(sender) + " tag <username>", NamedTextColor.RED));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Component.text("[OrbisCombat]: Player doesn't exist.", NamedTextColor.RED));
                return;
            }
        }

        CombatHandler.applyTag(target);
        sender.sendMessage(Component.text("[OrbisCombat]: " + target.getName() + " has been tagged.", NamedTextColor.GREEN));
    }

    private void parseUntagCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.combattag")) {
            sender.sendMessage(Component.text("[OrbisCombat]: You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        Player target;

        if (args.length < 2) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Component.text("[OrbisCombat]: Incorrect Usage: /" + getMainCommand(sender) + " untag <username>", NamedTextColor.RED));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Component.text("[OrbisCombat]: Player doesn't exist.", NamedTextColor.RED));
                return;
            }
        }

        if (!CombatHandler.isTagged(target)) {
            sender.sendMessage(Component.text("[OrbisCombat]: Player is not combat tagged.", NamedTextColor.RED));
            return;
        }

        CombatHandler.removeTag(target);
        sender.sendMessage(Component.text("[OrbisCombat]: " + target.getName() + " has been untagged.", NamedTextColor.GREEN));
    }

    private void parseStatusCommand(CommandSender sender, String[] args) {
        Player target;

        if (args.length < 2) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Component.text("[OrbisCombat]: You must specify a player name.", NamedTextColor.RED));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Component.text("[OrbisCombat]: Player doesn't exist.", NamedTextColor.RED));
                return;
            }
        }

        Resident resident = TownyAPI.getInstance().getResident(target);
        
        sender.sendMessage(Component.text("=== " + target.getName() + "'s Status ===", NamedTextColor.GOLD));
        
        // Combat tag status
        if (CombatHandler.isTagged(target)) {
            long remaining = CombatHandler.getRemaining(target);
            sender.sendMessage(Component.text("Combat Tagged: Yes (" + (remaining / 1000) + "s remaining)", NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.text("Combat Tagged: No", NamedTextColor.GREEN));
        }

        // New player protection status
        if (resident != null) {
            if (NewPlayerManager.hasNewPlayerProtection(resident)) {
                long remaining = NewPlayerManager.getRemainingProtectionTime(resident);
                long hours = remaining / (1000 * 60 * 60);
                long minutes = (remaining % (1000 * 60 * 60)) / (1000 * 60);
                sender.sendMessage(Component.text("New Player Protection: Active (" + hours + "h " + minutes + "m remaining)", NamedTextColor.BLUE));
            } else {
                sender.sendMessage(Component.text("New Player Protection: Inactive", NamedTextColor.GRAY));
            }
        }
    }

    private void parseProtectionCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("[OrbisCombat]: Only players can use this command!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("disable")) {
            sender.sendMessage(Component.text("[OrbisCombat]: Usage: /" + getMainCommand(sender) + " protection disable", NamedTextColor.RED));
            return;
        }

        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null) {
            player.sendMessage(Component.text("[OrbisCombat]: Unable to find your resident data!", NamedTextColor.RED));
            return;
        }

        if (!NewPlayerManager.hasNewPlayerProtection(resident)) {
            player.sendMessage(Component.text("[OrbisCombat]: You don't have new player protection active!", NamedTextColor.RED));
            return;
        }

        NewPlayerManager.disableProtection(resident);
        player.sendMessage(Component.text("[OrbisCombat]: Your new player protection has been disabled! You can now engage in PvP.", NamedTextColor.YELLOW));
    }

    private void parseReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.reload")) {
            sender.sendMessage(Component.text("[OrbisCombat]: You don't have permission to reload the config!", NamedTextColor.RED));
            return;
        }

        try {
            // Reload the configuration file
            EMCCOM.getInstance().reloadConfig();
            
            // Re-initialize config defaults (in case new values were added)
            Config.init(EMCCOM.getInstance().getConfig());
            
            // Save the config to ensure any new defaults are written to file
            EMCCOM.getInstance().saveConfig();
            
            // Refresh all player armor stats to apply new values
            if (EMCCOM.getInstance().getConfig().getBoolean("armor.enabled", true)) {
                ArmorManager.refreshAllPlayerArmor();
                sender.sendMessage(Component.text("[OrbisCombat]: Config reloaded! Armor stats refreshed for all players.", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("[OrbisCombat]: Config reloaded! (Armor system is disabled)", NamedTextColor.GREEN));
            }
            
            // Log the reload
            EMCCOM.getInstance().getLogger().info("Configuration reloaded by " + sender.getName());
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("[OrbisCombat]: Error reloading config: " + e.getMessage(), NamedTextColor.RED));
            EMCCOM.getInstance().getLogger().severe("Error reloading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "status", "protection");
            
            if (sender.hasPermission("emccom.command.combattag")) {
                subcommands = Arrays.asList("help", "tag", "untag", "status", "protection");
            }
            
            if (sender.hasPermission("emccom.command.reload")) {
                subcommands = new ArrayList<>(subcommands);
                subcommands.add("reload");
            }
            
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("tag") || args[0].equalsIgnoreCase("untag") || args[0].equalsIgnoreCase("status")) 
                && sender.hasPermission("emccom.command.combattag")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("protection")) {
                return List.of("disable").stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
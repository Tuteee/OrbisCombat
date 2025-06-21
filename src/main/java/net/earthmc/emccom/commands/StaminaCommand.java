package net.earthmc.emccom.commands;

import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.stamina.StaminaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

public class StaminaCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            if (sender instanceof Player player) {
                displayStaminaStatus(sender, player);
            } else {
                showHelp(sender);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> showHelp(sender);
            case "check" -> parseCheckCommand(sender, args);
            case "refresh" -> parseRefreshCommand(sender, args);
            default -> {
                sender.sendMessage(Component.text("[OrbisCombat]: Unknown subcommand. Use /stamina help", NamedTextColor.RED));
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Stamina System Help ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        sender.sendMessage(Component.text("/stamina - Show your stamina status", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/stamina check [player] - Check stamina status", NamedTextColor.YELLOW));
        
        if (sender.hasPermission("emccom.command.stamina.admin")) {
            sender.sendMessage(Component.text("/stamina refresh [player] - Force refresh stamina", NamedTextColor.YELLOW));
        }
        
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Stamina Info:", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("• Stamina only matters during PvP combat", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Actions like jumping, attacking, and blocking cost stamina", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Sprinting continuously drains stamina", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Stand still or walk to regenerate stamina", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Low stamina causes exhaustion effects", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Armor type affects stamina efficiency", NamedTextColor.GRAY));
    }

    private void parseCheckCommand(CommandSender sender, String[] args) {
        Player target;

        if (args.length < 2) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage(Component.text("[OrbisCombat]: You must specify a player name.", NamedTextColor.RED));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Component.text("[OrbisCombat]: Player not found or offline.", NamedTextColor.RED));
                return;
            }
        }

        displayStaminaStatus(sender, target);
    }

    private void parseRefreshCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.stamina.admin")) {
            sender.sendMessage(Component.text("[OrbisCombat]: You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        Player target;

        if (args.length < 2) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage(Component.text("[OrbisCombat]: You must specify a player name.", NamedTextColor.RED));
                return;
            }
        } else {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Component.text("[OrbisCombat]: Player not found or offline.", NamedTextColor.RED));
                return;
            }
        }

        if (CombatHandler.isTagged(target)) {
            StaminaManager.initializePlayer(target);
            sender.sendMessage(Component.text("[OrbisCombat]: Refreshed stamina for " + target.getName(), NamedTextColor.GREEN));
        } else {
            StaminaManager.removePlayer(target);
            sender.sendMessage(Component.text("[OrbisCombat]: Removed stamina tracking for " + target.getName() + " (not in combat)", NamedTextColor.YELLOW));
        }
    }

    private void displayStaminaStatus(CommandSender sender, Player target) {
        sender.sendMessage(Component.text("=== " + target.getName() + "'s Stamina Status ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        if (!CombatHandler.isTagged(target)) {
            sender.sendMessage(Component.text("Player is not in combat - stamina system inactive", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Stamina only matters during PvP combat!", NamedTextColor.YELLOW));
            return;
        }

        double stamina = StaminaManager.getStamina(target);
        double staminaPercent = StaminaManager.getStaminaPercentage(target);
        
        // Display stamina bar
        int barLength = 20;
        int filledBars = (int) (staminaPercent * barLength);
        
        StringBuilder staminaBar = new StringBuilder();
        NamedTextColor barColor;
        
        if (staminaPercent > 0.6) {
            barColor = NamedTextColor.GREEN;
        } else if (staminaPercent > 0.2) {
            barColor = NamedTextColor.YELLOW;
        } else {
            barColor = NamedTextColor.RED;
        }
        
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                staminaBar.append("█");
            } else {
                staminaBar.append("░");
            }
        }
        
        sender.sendMessage(Component.text("Stamina: ", NamedTextColor.AQUA)
            .append(Component.text(staminaBar.toString(), barColor))
            .append(Component.text(" " + String.format("%.1f", stamina) + "/100.0", NamedTextColor.GRAY)));
        
        // Status effects
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Status Effects:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        
        if (stamina <= 5.0) {
            sender.sendMessage(Component.text("• Very Exhausted: Severe speed reduction, weakness", NamedTextColor.RED));
            sender.sendMessage(Component.text("• Cannot sprint or perform most actions", NamedTextColor.RED));
        } else if (stamina <= 20.0) {
            sender.sendMessage(Component.text("• Exhausted: Moderate speed reduction, slower attacks", NamedTextColor.YELLOW));
        } else if (stamina < 50.0) {
            sender.sendMessage(Component.text("• Tired: Slightly reduced performance", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text("• Fresh: No penalties", NamedTextColor.GREEN));
        }
        
        // Player state
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Current State:", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Sprinting: " + (target.isSprinting() ? "Yes" : "No"), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("On Ground: " + (target.isOnGround() ? "Yes" : "No"), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Swimming: " + (target.isSwimming() ? "Yes" : "No"), NamedTextColor.GRAY));
        
        // Action costs
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Action Costs:", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        sender.sendMessage(Component.text("• Jump: 25 stamina", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Attack: 20 stamina", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Block: 10 stamina", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("• Sprint: 8 stamina/second", NamedTextColor.GRAY));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "check");
            
            if (sender.hasPermission("emccom.command.stamina.admin")) {
                subcommands = Arrays.asList("help", "check", "refresh");
            }
            
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("refresh"))) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
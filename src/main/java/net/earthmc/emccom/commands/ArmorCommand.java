package net.earthmc.emccom.commands;

import net.earthmc.emccom.armor.ArmorManager;
import net.earthmc.emccom.armor.ArmorStats;
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

public class ArmorCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> showHelp(sender);
            case "refresh" -> parseRefreshCommand(sender, args);
            default -> {
                sender.sendMessage(Component.text("[OrbisCombat]: Unknown subcommand. Use /armor help", NamedTextColor.RED));
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Armor System Help ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        if (sender.hasPermission("emccom.command.armor.refresh")) {
            sender.sendMessage(Component.text("/armor refresh [player] - Refresh armor calculations", NamedTextColor.YELLOW));
        }
        
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Armor Types:", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("• Light: High mobility, dodge chance, low protection", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("• Medium: Balanced stats", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("• Heavy: High protection, reduced mobility", NamedTextColor.RED));
        sender.sendMessage(Component.text("• Wizard: Magic-focused, enhanced mobility", NamedTextColor.LIGHT_PURPLE));
    }

    private void parseRefreshCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.armor.refresh")) {
            sender.sendMessage(Component.text("[OrbisCombat]: You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            // Refresh all players
            ArmorManager.refreshAllPlayerArmor();
            sender.sendMessage(Component.text("[OrbisCombat]: Refreshed armor stats for all online players.", NamedTextColor.GREEN));
        } else {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !target.isOnline()) {
                sender.sendMessage(Component.text("[OrbisCombat]: Player not found or offline.", NamedTextColor.RED));
                return;
            }

            ArmorManager.updatePlayerArmor(target);
            sender.sendMessage(Component.text("[OrbisCombat]: Refreshed armor stats for " + target.getName(), NamedTextColor.GREEN));
        }
    }

    private void displayArmorStats(CommandSender sender, Player target, ArmorStats stats) {
        sender.sendMessage(Component.text("=== " + target.getName() + "'s Armor Stats ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        // Basic info
        sender.sendMessage(Component.text("Armor Pieces: " + stats.getArmorPieces() + "/4", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Average Durability: " + String.format("%.1f%%", stats.getAverageDurability() * 100), 
            stats.getAverageDurability() > 0.5 ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        sender.sendMessage(Component.text(""));
        
        // Combat stats
        sender.sendMessage(Component.text("Combat Statistics:", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Protection: " + String.format("%.1f%%", stats.getTotalProtection() * 100), NamedTextColor.BLUE));
        
        if (stats.getDodgeChance() > 0) {
            sender.sendMessage(Component.text("Dodge Chance: " + String.format("%.1f%%", stats.getDodgeChance() * 100), NamedTextColor.GREEN));
        }
        
        sender.sendMessage(Component.text(""));
        
        // Movement stats
        sender.sendMessage(Component.text("Movement Statistics:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Speed: " + String.format("%.1f%%", stats.getSpeedMultiplier() * 100), 
            stats.getSpeedMultiplier() >= 1.0 ? NamedTextColor.GREEN : NamedTextColor.RED));
        sender.sendMessage(Component.text("Jump Height: " + String.format("%.1f%%", stats.getJumpMultiplier() * 100),
            stats.getJumpMultiplier() >= 1.0 ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        sender.sendMessage(Component.text(""));
        
        // Special effects
        sender.sendMessage(Component.text("Special Effects:", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        sender.sendMessage(Component.text("Swimming: " + (stats.canSwimEffectively() ? "Normal" : "Restricted"), 
            stats.canSwimEffectively() ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        if (stats.getHungerMultiplier() > 0) {
            sender.sendMessage(Component.text("Combat Hunger Drain: +" + String.format("%.1f%%", stats.getHungerMultiplier() * 100), NamedTextColor.YELLOW));
        }
        
        if (stats.getStaminaMultiplier() != 1.0) {
            sender.sendMessage(Component.text("Stamina: " + String.format("%.1f%%", stats.getStaminaMultiplier() * 100),
                stats.getStaminaMultiplier() >= 1.0 ? NamedTextColor.GREEN : NamedTextColor.RED));
        }
        
        // Warnings
        if (stats.getAverageDurability() < 0.5) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("⚠ Warning: Damaged armor provides reduced benefits!", NamedTextColor.RED, TextDecoration.BOLD));
        }
        
        if (stats.isWearingHeavyArmor()) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("⚡ Heavy Armor: Increased hunger drain in combat", NamedTextColor.GOLD));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help");
            
            if (sender.hasPermission("emccom.command.armor.refresh")) {
                subcommands = Arrays.asList("help", "refresh");
            }
            
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("refresh") && sender.hasPermission("emccom.command.armor.refresh")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
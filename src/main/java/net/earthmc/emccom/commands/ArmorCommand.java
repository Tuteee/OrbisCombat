package net.earthmc.emccom.commands;

import net.earthmc.emccom.EMCCOM;
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
import org.bukkit.configuration.file.FileConfiguration;
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
            if (sender instanceof Player player) {
                displayArmorStats(sender, player, ArmorManager.getArmorStats(player));
            } else {
                showHelp(sender);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> showHelp(sender);
            case "refresh" -> parseRefreshCommand(sender, args);
            case "stats" -> parseStatsCommand(sender, args);
            case "debug" -> parseDebugCommand(sender, args);
            case "configdebug" -> parseConfigDebugCommand(sender, args);
            default -> {
                sender.sendMessage(Component.text("[OrbisCombat]: Unknown subcommand. Use /armor help", NamedTextColor.RED));
            }
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Armor System Help ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        sender.sendMessage(Component.text("/armor - Show your armor stats", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/armor stats [player] - Show armor stats", NamedTextColor.YELLOW));
        
        if (sender.hasPermission("emccom.command.armor.refresh")) {
            sender.sendMessage(Component.text("/armor refresh [player] - Refresh armor calculations", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("/armor debug [player] - Show detailed debug info", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("/armor configdebug - Show config debug info", NamedTextColor.YELLOW));
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

    private void parseStatsCommand(CommandSender sender, String[] args) {
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

        ArmorStats stats = ArmorManager.getArmorStats(target);
        if (stats == null) {
            sender.sendMessage(Component.text("[OrbisCombat]: No armor stats found for " + target.getName(), NamedTextColor.RED));
            return;
        }

        displayArmorStats(sender, target, stats);
    }

    private void parseDebugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.armor.refresh")) {
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

        ArmorStats stats = ArmorManager.getArmorStats(target);
        displayDebugInfo(sender, target, stats);
    }

    private void parseConfigDebugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("emccom.command.armor.refresh")) {
            sender.sendMessage(Component.text("[OrbisCombat]: You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("=== Config Debug Info ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        FileConfiguration config = EMCCOM.getInstance().getConfig();
        
        // Check if armor section exists
        if (config.contains("armor")) {
            sender.sendMessage(Component.text("✓ armor section found", NamedTextColor.GREEN));
            
            // Check heavy section
            if (config.contains("armor.heavy")) {
                sender.sendMessage(Component.text("✓ armor.heavy section found", NamedTextColor.GREEN));
                
                // Check individual values
                sender.sendMessage(Component.text("armor.heavy.speed_multiplier: " + config.get("armor.heavy.speed_multiplier"), NamedTextColor.GRAY));
                sender.sendMessage(Component.text("armor.heavy.jump_multiplier: " + config.get("armor.heavy.jump_multiplier"), NamedTextColor.GRAY));
                sender.sendMessage(Component.text("armor.heavy.protection_multiplier: " + config.get("armor.heavy.protection_multiplier"), NamedTextColor.GRAY));
                
            } else {
                sender.sendMessage(Component.text("✗ armor.heavy section NOT found", NamedTextColor.RED));
            }
            
            // Show all armor keys
            sender.sendMessage(Component.text("All armor keys:", NamedTextColor.YELLOW));
            if (config.getConfigurationSection("armor") != null) {
                for (String key : config.getConfigurationSection("armor").getKeys(true)) {
                    Object value = config.get("armor." + key);
                    sender.sendMessage(Component.text("  armor." + key + " = " + value, NamedTextColor.GRAY));
                }
            }
            
        } else {
            sender.sendMessage(Component.text("✗ armor section NOT found", NamedTextColor.RED));
        }
        
        // Show file location
        sender.sendMessage(Component.text("Config file: " + EMCCOM.getInstance().getDataFolder().getAbsolutePath() + "/config.yml", NamedTextColor.AQUA));
    }

    private void displayArmorStats(CommandSender sender, Player target, ArmorStats stats) {
        if (stats == null) {
            sender.sendMessage(Component.text("=== " + target.getName() + "'s Armor Stats ===", NamedTextColor.GOLD, TextDecoration.BOLD));
            sender.sendMessage(Component.text("No armor stats available. Player may not be wearing any armor.", NamedTextColor.GRAY));
            return;
        }

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

    private void displayDebugInfo(CommandSender sender, Player target, ArmorStats stats) {
        sender.sendMessage(Component.text("=== Debug Info for " + target.getName() + " ===", NamedTextColor.GOLD, TextDecoration.BOLD));
        
        // Show armor contents with Nexo IDs
        sender.sendMessage(Component.text("Armor Contents:", NamedTextColor.AQUA));
        String[] slotNames = {"Boots", "Leggings", "Chestplate", "Helmet"};
        org.bukkit.inventory.ItemStack[] armor = target.getInventory().getArmorContents();
        
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null && !armor[i].getType().isAir()) {
                String nexoId = "None";
                try {
                    if (EMCCOM.getInstance().getServer().getPluginManager().isPluginEnabled("Nexo")) {
                        String id = com.nexomc.nexo.api.NexoItems.idFromItem(armor[i]);
                        if (id != null) {
                            nexoId = id;
                        }
                    }
                } catch (Exception e) {
                    nexoId = "Error: " + e.getMessage();
                }
                
                sender.sendMessage(Component.text("  " + slotNames[i] + ": " + armor[i].getType().name() + " (Nexo: " + nexoId + ")", NamedTextColor.GRAY));
            } else {
                sender.sendMessage(Component.text("  " + slotNames[i] + ": Empty", NamedTextColor.DARK_GRAY));
            }
        }
        
        if (stats != null) {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("Raw Values:", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("  Speed Multiplier: " + String.format("%.4f", stats.getSpeedMultiplier()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Jump Multiplier: " + String.format("%.4f", stats.getJumpMultiplier()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Protection: " + String.format("%.4f", stats.getTotalProtection()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Dodge Chance: " + String.format("%.4f", stats.getDodgeChance()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Hunger Multiplier: " + String.format("%.4f", stats.getHungerMultiplier()), NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("No armor stats calculated!", NamedTextColor.RED));
        }
        
        // Show current attribute values
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("Current Attributes:", NamedTextColor.AQUA));
        org.bukkit.attribute.AttributeInstance speedAttr = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
        org.bukkit.attribute.AttributeInstance jumpAttr = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_JUMP_STRENGTH);
        
        if (speedAttr != null) {
            sender.sendMessage(Component.text("  Movement Speed: " + String.format("%.4f", speedAttr.getValue()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Speed Modifiers: " + speedAttr.getModifiers().size(), NamedTextColor.GRAY));
        }
        
        if (jumpAttr != null) {
            sender.sendMessage(Component.text("  Jump Strength: " + String.format("%.4f", jumpAttr.getValue()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("  Jump Modifiers: " + jumpAttr.getModifiers().size(), NamedTextColor.GRAY));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("help", "stats");
            
            if (sender.hasPermission("emccom.command.armor.refresh")) {
                subcommands = Arrays.asList("help", "refresh", "stats", "debug", "configdebug");
            }
            
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("refresh") || args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("debug")) 
                && sender.hasPermission("emccom.command.armor.refresh")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}
package com.orbismc.itemloretowny.manager;

import com.orbismc.itemloretowny.TownyItemLore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandManager implements CommandExecutor {
    private final TownyItemLore plugin;

    public CommandManager(TownyItemLore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(player);
                break;
            case "playerscroll":
                if (player.hasPermission("townylore.scroll.player")) {
                    ItemStack scroll = plugin.getScrollManager().createPlayerKillScroll();
                    player.getInventory().addItem(scroll);
                    player.sendMessage("§aYou received a Player Kill Tracker Scroll.");
                } else {
                    player.sendMessage("§cYou don't have permission to use this command.");
                }
                break;
            case "mobscroll":
                if (player.hasPermission("townylore.scroll.mob")) {
                    ItemStack scroll = plugin.getScrollManager().createMobKillScroll();
                    player.getInventory().addItem(scroll);
                    player.sendMessage("§aYou received a Mob Kill Tracker Scroll.");
                } else {
                    player.sendMessage("§cYou don't have permission to use this command.");
                }
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== TownyItemLore Commands ===");
        player.sendMessage("§e/townylore help §7- Display this help menu");
        if (player.hasPermission("townylore.scroll.player")) {
            player.sendMessage("§e/townylore playerscroll §7- Get a player kill tracker scroll");
        }
        if (player.hasPermission("townylore.scroll.mob")) {
            player.sendMessage("§e/townylore mobscroll §7- Get a mob kill tracker scroll");
        }
    }
}
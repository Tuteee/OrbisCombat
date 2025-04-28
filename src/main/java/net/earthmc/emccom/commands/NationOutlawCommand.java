package net.earthmc.emccom.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import net.earthmc.emccom.manager.NationOutlawManager;
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
import java.util.List;
import java.util.stream.Collectors;

public class NationOutlawCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
            return true;
        }

        // Permission check
        if (!player.hasPermission("towny.command.nation.outlaw")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "add" -> {
                    handleAdd(player, args);
                }
                case "remove" -> {
                    handleRemove(player, args);
                }
                case "list" -> {
                    handleList(player, args);
                }
                default -> showHelp(player);
            }

        } catch (Exception e) {
            player.sendMessage(Component.text("An error occurred while executing the command.", NamedTextColor.RED));
            e.printStackTrace();
        }

        return true;
    }

    private void handleAdd(Player player, String[] args) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasNation()) {
            player.sendMessage(Component.text("You must be in a nation to use this command!", NamedTextColor.RED));
            return;
        }

        // Check if player has permission to manage nation outlaws
        if (!player.hasPermission("towny.command.nation.outlaw.manage")) {
            player.sendMessage(Component.text("You don't have permission to manage nation outlaws!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /n outlaw add <player>", NamedTextColor.RED));
            return;
        }

        try {
            Nation nation = resident.getNation();
            String targetName = args[1];
            Resident targetResident = TownyAPI.getInstance().getResident(targetName);

            if (targetResident == null) {
                player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            if (NationOutlawManager.hasOutlaw(nation, targetResident)) {
                player.sendMessage(Component.text(targetResident.getName() + " is already outlawed in your nation!", NamedTextColor.RED));
                return;
            }

            NationOutlawManager.addOutlaw(nation, targetResident);
            player.sendMessage(Component.text("Successfully added " + targetResident.getName() + " to your nation's outlaw list!", NamedTextColor.GREEN));

            // Notify the outlawed player if they're online
            Player targetPlayer = Bukkit.getPlayer(targetResident.getName());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(Component.text("You have been outlawed by " + nation.getName() + "!", NamedTextColor.RED));
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("An error occurred while adding the outlaw.", NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void handleRemove(Player player, String[] args) {
        Resident resident = TownyAPI.getInstance().getResident(player);
        if (resident == null || !resident.hasNation()) {
            player.sendMessage(Component.text("You must be in a nation to use this command!", NamedTextColor.RED));
            return;
        }

        // Check if player has permission to manage nation outlaws
        if (!player.hasPermission("towny.command.nation.outlaw.manage")) {
            player.sendMessage(Component.text("You don't have permission to manage nation outlaws!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /n outlaw remove <player>", NamedTextColor.RED));
            return;
        }

        try {
            Nation nation = resident.getNation();
            String targetName = args[1];
            Resident targetResident = TownyAPI.getInstance().getResident(targetName);

            if (targetResident == null) {
                player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            if (!NationOutlawManager.hasOutlaw(nation, targetResident)) {
                player.sendMessage(Component.text(targetResident.getName() + " is not outlawed in your nation!", NamedTextColor.RED));
                return;
            }

            NationOutlawManager.removeOutlaw(nation, targetResident);
            player.sendMessage(Component.text("Successfully removed " + targetResident.getName() + " from your nation's outlaw list!", NamedTextColor.GREEN));
        } catch (Exception e) {
            player.sendMessage(Component.text("An error occurred while removing the outlaw.", NamedTextColor.RED));
            e.printStackTrace();
        }
    }

    private void handleList(Player player, String[] args) {
        Nation nation;

        if (args.length > 1) {
            // Looking up another nation's outlaws
            nation = TownyAPI.getInstance().getNation(args[1]);
            if (nation == null) {
                player.sendMessage(Component.text("Nation not found!", NamedTextColor.RED));
                return;
            }
        } else {
            // Looking up own nation's outlaws
            Resident resident = TownyAPI.getInstance().getResident(player);
            if (resident == null || !resident.hasNation()) {
                player.sendMessage(Component.text("You must specify a nation name!", NamedTextColor.RED));
                return;
            }
            try {
                nation = resident.getNation();
            } catch (Exception e) {
                player.sendMessage(Component.text("Error getting nation information.", NamedTextColor.RED));
                return;
            }
        }

        List<String> outlaws = NationOutlawManager.getOutlaws(nation);

        if (outlaws.isEmpty()) {
            player.sendMessage(Component.text(nation.getName() + " has no outlaws.", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text(nation.getName() + "'s Outlaws:", NamedTextColor.GOLD));
        outlaws.forEach(name ->
                player.sendMessage(Component.text("- " + name, NamedTextColor.YELLOW)));
    }

    private void showHelp(Player player) {
        player.sendMessage(Component.text("Nation Outlaw Commands:", NamedTextColor.GOLD));
        player.sendMessage(Component.text("- /n outlaw add <player> - Add a player to your nation's outlaw list", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("- /n outlaw remove <player> - Remove a player from your nation's outlaw list", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("- /n outlaw list [nation] - View a nation's outlaw list", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!sender.hasPermission("towny.command.nation.outlaw")) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("add");
            completions.add("remove");
            completions.add("list");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("list")) {
                return TownyAPI.getInstance().getNations().stream()
                        .map(Nation::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}
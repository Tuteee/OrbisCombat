package net.earthmc.emccom.combat.listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.*;
import com.palmergames.util.TimeTools;
import net.earthmc.emccom.combat.CombatHandler;
import net.earthmc.emccom.combat.bossbar.BossBarTask;
import net.earthmc.emccom.manager.NationOutlawManager;
import net.earthmc.emccom.manager.NewPlayerManager;
import net.earthmc.emccom.manager.ResidentMetadataManager;
import net.earthmc.emccom.object.CombatPref;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

import static net.earthmc.emccom.object.CombatPref.UNSAFE;

public class CombatListener implements Listener {

    List<String> messagesList = Arrays.asList(
            "used Combat Log! It's a One-Hit KO!",
            "was killed for logging out in combat.",
            "surrendered to the disconnect button.",
            "combat-logged! Shame on them!"
    );

    Random random = new Random();
    CombatLogMessages messageSelector = new CombatLogMessages(random, messagesList);

    public static final long TAG_TIME = 30 * 1000;
    public final static int effectDurationTicks = (int)(TimeTools.convertToTicks(TAG_TIME/1000));

    private Set<UUID> deathsForLoggingOut = new HashSet<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Resident resident = TownyAPI.getInstance().getResident(player);
        
        if (resident != null) {
            // Mark first join for new players
            NewPlayerManager.markFirstJoin(resident);
            
            // Send protection status message if they have protection
            if (NewPlayerManager.hasNewPlayerProtection(resident)) {
                long remainingTime = NewPlayerManager.getRemainingProtectionTime(resident);
                long hours = remainingTime / (1000 * 60 * 60);
                long minutes = (remainingTime % (1000 * 60 * 60)) / (1000 * 60);
                
                player.sendMessage(Component.text("=== New Player Protection ===", NamedTextColor.GOLD));
                player.sendMessage(Component.text("You are protected from PvP for " + hours + "h " + minutes + "m", NamedTextColor.GREEN));
                player.sendMessage(Component.text("Use '/orbiscombat protection disable' to disable protection early", NamedTextColor.YELLOW));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPvP(TownyPlayerDamagePlayerEvent event) {
        if (!event.isCancelled())
            return;

        TownyWorld world = TownyAPI.getInstance().getTownyWorld(event.getVictimPlayer().getWorld().getName());
        Player attacker = event.getAttackingPlayer();
        Player victim = event.getVictimPlayer();

        if (world == null || !world.isUsingTowny() || !world.isPVP()) {
            return;
        }

        // Get residents for checks
        Resident attackerResident = TownyAPI.getInstance().getResident(attacker);
        Resident victimResident = TownyAPI.getInstance().getResident(victim);

        if (attackerResident == null || victimResident == null) {
            return;
        }

        // Check for new player protection
        if (NewPlayerManager.hasNewPlayerProtection(attackerResident)) {
            attacker.sendMessage(Component.text("You cannot attack players while you have new player protection! Use '/orbiscombat protection disable' to disable it.", NamedTextColor.RED));
            return;
        }

        if (NewPlayerManager.hasNewPlayerProtection(victimResident)) {
            attacker.sendMessage(Component.text(victim.getName() + " has new player protection and cannot be attacked!", NamedTextColor.RED));
            return;
        }

        // Check if either player is combat tagged
        boolean isTagged = CombatHandler.isTagged(victim) || CombatHandler.isTagged(attacker);

        // Check for mutual nation enemies
        boolean areMutualEnemies = false;
        if (attackerResident.hasNation() && victimResident.hasNation()) {
            try {
                boolean attackerHasVictimAsEnemy = attackerResident.getNation().hasEnemy(victimResident.getNation());
                boolean victimHasAttackerAsEnemy = victimResident.getNation().hasEnemy(attackerResident.getNation());
                areMutualEnemies = attackerHasVictimAsEnemy && victimHasAttackerAsEnemy;
            } catch (Exception e) {
                // Handle the exception silently - assume nations aren't enemies
            }
        }

        // Check for outlaw combat permission (both town and nation)
        boolean canFightOutlaw = false;
        try {
            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(victim.getLocation());
            if (townBlock != null && townBlock.hasTown()) {
                Town town = townBlock.getTown();

                // Town outlaw checks
                // Case 1: Permission holder attacking a town outlaw
                if (attackerResident.hasTown() &&
                        attackerResident.getTown().equals(town) &&
                        attacker.hasPermission("towny.outlaw.combat") &&
                        town.hasOutlaw(victimResident)) {
                    canFightOutlaw = true;
                }

                // Case 2: Town outlaw fighting back against a permission holder
                if (town.hasOutlaw(attackerResident) &&
                        victimResident.hasTown() &&
                        victimResident.getTown().equals(town) &&
                        victim.hasPermission("towny.outlaw.combat")) {
                    canFightOutlaw = true;
                }

                // Nation outlaw checks
                if (town.hasNation()) {
                    Nation townNation = town.getNation();

                    // Case 1: Permission holder attacking a nation outlaw
                    if (attackerResident.hasNation() &&
                            attackerResident.getNation().equals(townNation) &&
                            attacker.hasPermission("towny.nation.outlaw.combat") &&
                            NationOutlawManager.hasOutlaw(townNation, victimResident)) {
                        canFightOutlaw = true;
                    }

                    // Case 2: Nation outlaw fighting back against a permission holder
                    if (NationOutlawManager.hasOutlaw(townNation, attackerResident) &&
                            victimResident.hasNation() &&
                            victimResident.getNation().equals(townNation) &&
                            victim.hasPermission("towny.nation.outlaw.combat")) {
                        canFightOutlaw = true;
                    }
                }

                // Case 3: Combat is already ongoing between these players
                if ((CombatHandler.isTagged(attacker) &&
                        (town.hasOutlaw(attackerResident) ||
                                (town.hasNation() && NationOutlawManager.hasOutlaw(town.getNation(), attackerResident)))) ||
                        (CombatHandler.isTagged(victim) &&
                                (town.hasOutlaw(victimResident) ||
                                        (town.hasNation() && NationOutlawManager.hasOutlaw(town.getNation(), victimResident))))) {
                    canFightOutlaw = true;
                }
            }
        } catch (Exception e) {
            // Handle the exception silently
        }

        // Allow combat if:
        // 1. Players are from mutually enemied nations OR
        // 2. One player is tagged and attacker is UNSAFE or already tagged OR
        // 3. Either player has permission and other is outlawed (or fighting back)
        if (areMutualEnemies ||
                (isTagged && (ResidentMetadataManager.getResidentCombatPref(attackerResident) == UNSAFE || CombatHandler.isTagged(attacker))) ||
                canFightOutlaw) {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player damaged))
            return;

        Entity damagingEntity = event.getDamager();
        if (damagingEntity instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter)
            damagingEntity = shooter;

        if (damagingEntity instanceof Player damager && !damager.equals(damaged)) {
            CombatHandler.applyTag(damager);
            CombatHandler.applyTag(damaged);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!CombatHandler.isTagged(player))
            return;

        BossBarTask.remove(player);
        CombatHandler.removeTag(player);

        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player.getLocation());
        if (townBlock != null && townBlock.getType() == TownBlockType.ARENA && townBlock.hasTown())
            return;

        deathsForLoggingOut.add(player.getUniqueId());
        player.setHealth(0.0);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (deathsForLoggingOut.contains(player.getUniqueId())) {
            deathsForLoggingOut.remove(player.getUniqueId());
            event.deathMessage(Component.text(player.getName() + " " + messageSelector.getRandomMessage()));
        }

        if (!CombatHandler.isTagged(player))
            return;

        CombatHandler.removeTag(player);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();
        if (!(killer == null)) {
            BossBarTask.remove(killer);
            killer.sendMessage(ChatColor.GREEN + "Your enemy is dead. You are no longer in combat.");
            CombatHandler.removeTag(killer);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST)
            return;

        if (!(event.getPlayer() instanceof Player))
            return;

        Player player = (Player) event.getPlayer();

        if (!CombatHandler.isTagged(player))
            return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You can't use your ender chest while being in combat.");
    }

    @EventHandler
    public void onRiptide(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!CombatHandler.isTagged(player))
            return;

        if (!player.isRiptiding())
            return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "The riptide enchantment is disabled in combat.");
    }

    @EventHandler
    public void onElytraFly(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!CombatHandler.isTagged(player))
            return;
        if (!player.isGliding())
            return;
        event.setCancelled(true);
        player.sendMessage((ChatColor.RED + "Elytras aren't enabled in combat."));
    }
}
package com.orbismc.itemloretowny.listener;

import com.orbismc.itemloretowny.tracker.KillTracker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityDeathListener implements Listener {
    private final KillTracker killTracker;

    public EntityDeathListener(KillTracker killTracker) {
        this.killTracker = killTracker;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();

        if (weapon == null || weapon.getType().isAir()) return;

        if (!killTracker.hasKillTracker(weapon)) return;

        if (entity instanceof Player) {
            killTracker.incrementPlayerKills(weapon);
        } else {
            killTracker.incrementMobKills(weapon);
        }
    }
}
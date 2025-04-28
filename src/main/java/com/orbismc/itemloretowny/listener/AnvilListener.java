package com.orbismc.itemloretowny.listener;

import com.orbismc.itemloretowny.manager.ScrollManager;
import com.orbismc.itemloretowny.tracker.KillTracker;
import com.orbismc.itemloretowny.util.MaterialUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {
    private final ScrollManager scrollManager;
    private final KillTracker killTracker;

    public AnvilListener(ScrollManager scrollManager, KillTracker killTracker) {
        this.scrollManager = scrollManager;
        this.killTracker = killTracker;
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack first = inventory.getFirstItem();
        ItemStack second = inventory.getSecondItem();

        if (first == null || second == null) return;

        // Check if first item is a weapon
        if (!MaterialUtil.isWeapon(first.getType())) return;

        // Check if second item is a kill scroll
        if (scrollManager.isPlayerKillScroll(second)) {
            ItemStack result = first.clone();
            boolean applied = killTracker.addTrackerToItem(result, KillTracker.TrackerType.PLAYER);

            if (applied) {
                event.setResult(result);
            }
        } else if (scrollManager.isMobKillScroll(second)) {
            ItemStack result = first.clone();
            boolean applied = killTracker.addTrackerToItem(result, KillTracker.TrackerType.MOB);

            if (applied) {
                event.setResult(result);
            }
        }
    }
}
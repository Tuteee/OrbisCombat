package net.earthmc.emccom.combat;

import net.earthmc.emccom.EMCCOM;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CombatHandler {
    public static final long TAG_TIME = 30 * 1000;
    private static final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();

    public static void startTask(EMCCOM plugin) {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> {
            Iterator<Entry<UUID, Long>> iterator = combatTags.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<UUID, Long> entry = iterator.next();
                if (entry.getValue() > System.currentTimeMillis())
                    continue;
                iterator.remove();
                UUID uuid = entry.getKey();
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline())
                    continue;
                player.sendMessage(Component.text("You are no longer in combat.", NamedTextColor.GREEN));
            }
        }, 500L, 500L, TimeUnit.MILLISECONDS);
    }

    public static void applyTag(Player player) {
        if (!isTagged(player)) {
            player.closeInventory(Reason.PLUGIN);
            player.sendMessage(Component.text("You have been combat tagged for " + (TAG_TIME / 1000) + " seconds! Do not log out or you will get killed instantly.", NamedTextColor.RED));
        }
        combatTags.put(player.getUniqueId(), System.currentTimeMillis() + TAG_TIME);
    }

    public static void removeTag(Player player) {
        combatTags.remove(player.getUniqueId());
    }

    public static boolean isTagged(Player player) {
        Long untagTime = combatTags.get(player.getUniqueId());
        return untagTime != null && untagTime > System.currentTimeMillis();
    }

    public static long getRemaining(Player player) {
        if (!combatTags.containsKey(player.getUniqueId()))
            return -1;
        return combatTags.get(player.getUniqueId()) - System.currentTimeMillis();
    }
}
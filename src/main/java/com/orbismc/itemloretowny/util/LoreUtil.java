package com.orbismc.itemloretowny.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LoreUtil {
    // Pattern to identify kill counter lines
    private static final Pattern PLAYER_KILLS_PATTERN = Pattern.compile(".*[Pp]layers [Ss]lain.*");
    private static final Pattern MOB_KILLS_PATTERN = Pattern.compile(".*[Mm]onsters [Ss]lain.*");

    public static List<Component> generateLore(Player player) {
        List<Component> lore = new ArrayList<>();

        lore.add(parseMiniMessage("<dark_gray>ʜɪsᴛᴏʀɪᴄᴀʟ ɪɴғᴏʀᴍᴀᴛɪᴏɴ"));
        addForgedByToLore(player, lore);
        addOriginToLore(player, lore);
        lore.add(parseMiniMessage("<!i><gray>ᴅᴀᴛᴇ: " + (TimeUtil.formattedDate)));

        return lore;
    }

    public static void addForgedByToLore(Player player, List<Component> lore) {
        String playerName = player.getName();
        String forgedByMessage = "<!i><gray>ғᴏʀɢᴇᴅ ʙʏ: ";
        lore.add(parseMiniMessage(forgedByMessage + playerName));
    }

    public static void addOriginToLore(Player player, List<Component> lore) {
        String originMessage = "<!i><gray>ᴏʀɪɢɪɴᴀᴛᴇᴅ ɪɴ: ";
        String unknownOrigin = "ᴜɴᴋɴᴏᴡɴ";
        String wildernessOrigin = "<!i><green>ᴡɪʟᴅᴇʀɴᴇss ☠";
        String darkAqua = "<dark_aqua>";
        String gold = "<gold>";
        String gray = "<gray>";

        if (!TownyAPI.getInstance().isTownyWorld(player.getWorld())) {
            lore.add(parseMiniMessage(originMessage + unknownOrigin));
        } else {
            Town town = TownyAPI.getInstance().getTown(player.getLocation());
            if (TownyAPI.getInstance().isWilderness(player.getLocation())) {
                lore.add(parseMiniMessage(originMessage + wildernessOrigin));
            } else {
                assert town != null;
                Nation nation = town.getNationOrNull();
                String townName = darkAqua + town.getName() + gray;
                if (nation == null) {
                    lore.add(parseMiniMessage(originMessage + "[" + townName + "]"));
                } else {
                    String nationName = gold + nation.getName() + gray;
                    lore.add(parseMiniMessage(originMessage + "[" + nationName + "|" + townName + "]"));
                }
            }
        }
    }

    public static void updateKillsLore(ItemStack item, int playerKills, int mobKills) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<Component> existingLore = meta.lore();

        if (existingLore == null) {
            existingLore = new ArrayList<>();
        }

        // Create a new list without the kill counter lines
        List<Component> newLore = new ArrayList<>();
        for (Component line : existingLore) {
            String plainText = line.toString();
            if (!PLAYER_KILLS_PATTERN.matcher(plainText).matches() &&
                    !MOB_KILLS_PATTERN.matcher(plainText).matches()) {
                newLore.add(line);
            }
        }

        // Add kill counters if they exist
        if (playerKills > 0) {
            newLore.add(parseMiniMessage("<dark_red>ᴘʟᴀʏᴇʀs sʟᴀɪɴ: " + playerKills));
        }

        if (mobKills > 0) {
            newLore.add(parseMiniMessage("<dark_green>ᴍᴏɴsᴛᴇʀs sʟᴀɪɴ: " + mobKills));
        }

        meta.lore(newLore);
        item.setItemMeta(meta);
    }

    public static Component parseMiniMessage(String message) {
        MiniMessage miniMessage = MiniMessage.builder().build();
        return miniMessage.deserialize(message);
    }
}
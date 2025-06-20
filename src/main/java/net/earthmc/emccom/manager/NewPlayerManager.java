package net.earthmc.emccom.manager;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import net.earthmc.emccom.EMCCOM;
import org.bukkit.entity.Player;

public class NewPlayerManager {
    private static final String FIRST_JOIN_KEY = "emccom_first_join";
    private static final String PROTECTION_DISABLED_KEY = "emccom_protection_disabled";

    private NewPlayerManager() {}

    public static void markFirstJoin(Resident resident) {
        if (!resident.hasMeta(FIRST_JOIN_KEY)) {
            resident.addMetaData(new LongDataField(FIRST_JOIN_KEY, System.currentTimeMillis()));
            resident.save();
        }
    }

    public static boolean hasNewPlayerProtection(Resident resident) {
        // Check if protection is manually disabled
        if (resident.hasMeta(PROTECTION_DISABLED_KEY)) {
            StringDataField sdf = (StringDataField) resident.getMetadata(PROTECTION_DISABLED_KEY);
            if (sdf != null && "true".equals(sdf.getValue())) {
                return false;
            }
        }

        // Check if player is within protection time
        if (!resident.hasMeta(FIRST_JOIN_KEY)) {
            // If no first join data, assume they're not new
            return false;
        }

        LongDataField ldf = (LongDataField) resident.getMetadata(FIRST_JOIN_KEY);
        if (ldf == null) {
            return false;
        }

        long firstJoinTime = ldf.getValue();
        long protectionHours = EMCCOM.getInstance().getConfig().getLong("new_player_protection_hours", 24);
        long protectionDuration = protectionHours * 60 * 60 * 1000; // Convert to milliseconds

        return (System.currentTimeMillis() - firstJoinTime) < protectionDuration;
    }

    public static void disableProtection(Resident resident) {
        if (!resident.hasMeta(PROTECTION_DISABLED_KEY)) {
            resident.addMetaData(new StringDataField(PROTECTION_DISABLED_KEY, "true"));
        } else {
            StringDataField sdf = (StringDataField) resident.getMetadata(PROTECTION_DISABLED_KEY);
            if (sdf != null) {
                sdf.setValue("true");
                resident.addMetaData(sdf);
            }
        }
        resident.save();
    }

    public static long getRemainingProtectionTime(Resident resident) {
        if (!hasNewPlayerProtection(resident)) {
            return 0;
        }

        LongDataField ldf = (LongDataField) resident.getMetadata(FIRST_JOIN_KEY);
        if (ldf == null) {
            return 0;
        }

        long firstJoinTime = ldf.getValue();
        long protectionHours = EMCCOM.getInstance().getConfig().getLong("new_player_protection_hours", 24);
        long protectionDuration = protectionHours * 60 * 60 * 1000;
        long remainingTime = protectionDuration - (System.currentTimeMillis() - firstJoinTime);

        return Math.max(0, remainingTime);
    }
}
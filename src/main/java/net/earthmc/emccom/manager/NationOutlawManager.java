package net.earthmc.emccom.manager;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NationOutlawManager {
    private static final String OUTLAWS_KEY = "emccom_nation_outlaws";

    private NationOutlawManager() {}

    public static void addOutlaw(Nation nation, Resident resident) {
        List<String> outlaws = getOutlaws(nation);
        if (!outlaws.contains(resident.getName())) {
            outlaws.add(resident.getName());
            saveOutlaws(nation, outlaws);
        }
    }

    public static void removeOutlaw(Nation nation, Resident resident) {
        List<String> outlaws = getOutlaws(nation);
        outlaws.remove(resident.getName());
        saveOutlaws(nation, outlaws);
    }

    public static boolean hasOutlaw(Nation nation, Resident resident) {
        return getOutlaws(nation).contains(resident.getName());
    }

    public static List<String> getOutlaws(Nation nation) {
        if (!nation.hasMeta(OUTLAWS_KEY)) {
            return new ArrayList<>();
        }

        StringDataField sdf = (StringDataField) nation.getMetadata(OUTLAWS_KEY);
        if (sdf == null || sdf.getValue().isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(sdf.getValue().split(",")));
    }

    private static void saveOutlaws(Nation nation, List<String> outlaws) {
        String outlawString = String.join(",", outlaws);

        if (!nation.hasMeta(OUTLAWS_KEY)) {
            nation.addMetaData(new StringDataField(OUTLAWS_KEY, outlawString));
        } else {
            StringDataField sdf = (StringDataField) nation.getMetadata(OUTLAWS_KEY);
            if (sdf != null) {
                sdf.setValue(outlawString);
                nation.addMetaData(sdf);
            }
        }
        nation.save();
    }
}
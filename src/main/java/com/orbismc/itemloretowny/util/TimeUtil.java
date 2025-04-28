package com.orbismc.itemloretowny.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd]");
    static Date currentDate = new Date();
    public static String formattedDate = dateFormat.format(currentDate);
}
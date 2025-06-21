package net.earthmc.emccom.stamina.task;

import net.earthmc.emccom.stamina.StaminaManager;
import org.bukkit.scheduler.BukkitRunnable;

public class StaminaTask extends BukkitRunnable {

    @Override
    public void run() {
        StaminaManager.updateAllPlayerStamina();
    }
}
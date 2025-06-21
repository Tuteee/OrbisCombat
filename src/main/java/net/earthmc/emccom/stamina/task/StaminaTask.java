package net.earthmc.emccom.stamina.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.earthmc.emccom.stamina.StaminaManager;

import java.util.function.Consumer;

public class StaminaTask implements Consumer<ScheduledTask> {

    @Override
    public void accept(ScheduledTask task) {
        StaminaManager.updateAllPlayerStamina();
    }
}
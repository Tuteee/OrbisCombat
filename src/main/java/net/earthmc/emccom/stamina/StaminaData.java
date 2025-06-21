package net.earthmc.emccom.stamina;

import org.bukkit.Location;

public class StaminaData {
    public double stamina;
    public long lastUpdate;
    public Location lastLocation;
    public long lastDamageTime;
    public boolean wasMovingLastTick;
    public boolean wasSprintingLastTick;
    
    public StaminaData() {
        this.stamina = 100.0; // Start with full stamina
        this.lastUpdate = System.currentTimeMillis();
        this.lastLocation = null;
        this.lastDamageTime = 0;
        this.wasMovingLastTick = false;
        this.wasSprintingLastTick = false;
    }
    
    public void updatePosition(Location location) {
        this.lastLocation = location.clone();
    }
    
    public void recordDamage() {
        this.lastDamageTime = System.currentTimeMillis();
    }
    
    public boolean wasRecentlyDamaged() {
        long damageDelay = net.earthmc.emccom.EMCCOM.getInstance().getConfig().getLong("stamina.damage_regen_delay_ms", 1000);
        return (System.currentTimeMillis() - lastDamageTime) < damageDelay;
    }
    
    public boolean isExhausted() {
        return stamina < 20.0;
    }
    
    public boolean isVeryExhausted() {
        return stamina < 5.0;
    }
}
package net.earthmc.emccom.stamina;

import net.earthmc.emccom.EMCCOM;
import net.earthmc.emccom.armor.ArmorManager;
import net.earthmc.emccom.combat.CombatHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StaminaManager {
    private static final Map<UUID, StaminaData> playerStamina = new ConcurrentHashMap<>();
    private static final NamespacedKey STAMINA_SPEED_MODIFIER_KEY = new NamespacedKey(EMCCOM.getInstance(), "stamina_speed");
    
    // Stamina configuration - matches your config.yml values
    private static final double MAX_STAMINA = 100.0;
    private static final double STAMINA_REGEN_RATE = 0.3; // Per second when standing still
    private static final double STAMINA_REGEN_WALKING = 0.2; // Per second when walking (not sprinting)
    
    // Stamina costs (per action) - matches your config.yml values
    private static final double SPRINT_COST_PER_SECOND = 8.0;
    private static final double JUMP_COST = 25.0;
    private static final double ATTACK_COST = 20.0;
    private static final double BLOCK_COST = 10.0;
    
    // Exhaustion thresholds - matches your config.yml values
    private static final double EXHAUSTED_THRESHOLD = 20.0;
    private static final double VERY_EXHAUSTED_THRESHOLD = 5.0;

    public static void updatePlayerStamina(Player player) {
        if (!CombatHandler.isTagged(player)) {
            // Remove stamina tracking when not in combat
            removePlayer(player);
            return;
        }

        StaminaData data = playerStamina.computeIfAbsent(player.getUniqueId(), k -> new StaminaData());
        
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - data.lastUpdate) / 1000.0; // Convert to seconds
        
        // Don't process if delta time is too large (player just joined combat) or too small
        if (deltaTime > 2.0 || deltaTime < 0.05) {
            data.lastUpdate = currentTime;
            return;
        }
        
        data.lastUpdate = currentTime;
        
        double armorStaminaMultiplier = ArmorManager.getStaminaMultiplier(player);
        
        // Check if player is actually moving
        boolean isMoving = isPlayerMoving(player, data);
        boolean isStandingStill = !isMoving;
        
        // Calculate stamina regeneration based on player state
        double regenRate = 0.0;
        
        // No regeneration if recently damaged
        long damageDelay = EMCCOM.getInstance().getConfig().getLong("stamina.damage_regen_delay_ms", 1000);
        if ((currentTime - data.lastDamageTime) < damageDelay) {
            // No regen if recently damaged
            regenRate = 0.0;
        } else if (isStandingStill && !player.isSprinting()) {
            regenRate = STAMINA_REGEN_RATE * armorStaminaMultiplier;
        } else if (isMoving && !player.isSprinting()) {
            regenRate = STAMINA_REGEN_WALKING * armorStaminaMultiplier;
        }
        // No regen while sprinting
        
        // Apply stamina regeneration
        if (regenRate > 0) {
            data.stamina = Math.min(MAX_STAMINA, data.stamina + (regenRate * deltaTime));
        }
        
        // Apply sprint cost if sprinting
        if (player.isSprinting()) {
            double sprintCost = (SPRINT_COST_PER_SECOND / armorStaminaMultiplier) * deltaTime;
            data.stamina = Math.max(0, data.stamina - sprintCost);
        }
        
        // Update tracking data
        data.updatePosition(player.getLocation());
        data.wasMovingLastTick = isMoving;
        data.wasSprintingLastTick = player.isSprinting();
        
        // Apply stamina effects
        applyStaminaEffects(player, data);
        
        // Send stamina action bar
        sendStaminaActionBar(player, data);
    }

    public static void consumeStamina(Player player, StaminaAction action) {
        if (!CombatHandler.isTagged(player)) {
            return; // Only consume stamina in combat
        }

        StaminaData data = playerStamina.get(player.getUniqueId());
        if (data == null) {
            return;
        }

        double armorStaminaMultiplier = ArmorManager.getStaminaMultiplier(player);
        double cost = switch (action) {
            case JUMP -> JUMP_COST / armorStaminaMultiplier;
            case ATTACK -> ATTACK_COST / armorStaminaMultiplier;
            case BLOCK -> BLOCK_COST / armorStaminaMultiplier;
        };

        data.stamina = Math.max(0, data.stamina - cost);
        applyStaminaEffects(player, data);
        sendStaminaActionBar(player, data);
    }

    public static boolean canPerformAction(Player player, StaminaAction action) {
        if (!CombatHandler.isTagged(player)) {
            return true; // No restrictions outside combat
        }

        StaminaData data = playerStamina.get(player.getUniqueId());
        if (data == null) {
            return true;
        }

        double armorStaminaMultiplier = ArmorManager.getStaminaMultiplier(player);
        double requiredStamina = switch (action) {
            case JUMP -> JUMP_COST / armorStaminaMultiplier;
            case ATTACK -> ATTACK_COST / armorStaminaMultiplier;
            case BLOCK -> BLOCK_COST / armorStaminaMultiplier;
        };

        return data.stamina >= requiredStamina;
    }

    public static double getStamina(Player player) {
        StaminaData data = playerStamina.get(player.getUniqueId());
        return data != null ? data.stamina : MAX_STAMINA;
    }

    public static double getStaminaPercentage(Player player) {
        return getStamina(player) / MAX_STAMINA;
    }

    public static void removePlayer(Player player) {
        playerStamina.remove(player.getUniqueId());
        removeStaminaEffects(player);
    }

    public static void initializePlayer(Player player) {
        if (CombatHandler.isTagged(player)) {
            playerStamina.put(player.getUniqueId(), new StaminaData());
        }
    }

    public static void recordDamage(Player player) {
        StaminaData data = playerStamina.get(player.getUniqueId());
        if (data != null) {
            data.recordDamage();
        }
    }

    private static boolean isPlayerMoving(Player player, StaminaData data) {
        if (data.lastLocation == null) {
            return false;
        }
        
        // Consider player moving if they've moved more than 0.1 blocks horizontally
        double distance = Math.sqrt(
            Math.pow(data.lastLocation.getX() - player.getLocation().getX(), 2) +
            Math.pow(data.lastLocation.getZ() - player.getLocation().getZ(), 2)
        );
        
        return distance > 0.05;
    }

    private static void applyStaminaEffects(Player player, StaminaData data) {
        // Remove existing stamina effects first
        removeStaminaEffects(player);
        
        double staminaPercent = data.stamina / MAX_STAMINA;
        
        if (staminaPercent <= (VERY_EXHAUSTED_THRESHOLD / MAX_STAMINA)) {
            // Very exhausted - severe penalties
            applySevereExhaustion(player);
        } else if (staminaPercent <= (EXHAUSTED_THRESHOLD / MAX_STAMINA)) {
            // Exhausted - moderate penalties
            applyModerateExhaustion(player);
        }
        
        // Prevent sprinting if stamina is too low
        if (data.stamina < 10.0 && player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    private static void applySevereExhaustion(Player player) {
        // Apply severe movement speed reduction
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            AttributeModifier modifier = new AttributeModifier(
                STAMINA_SPEED_MODIFIER_KEY,
                -0.4, // 40% speed reduction
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            speedAttribute.addModifier(modifier);
        }
        
        // Apply weakness effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, false, false));
        
        // Prevent sprinting
        if (player.isSprinting()) {
            player.setSprinting(false);
        }
    }

    private static void applyModerateExhaustion(Player player) {
        // Apply moderate movement speed reduction
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            AttributeModifier modifier = new AttributeModifier(
                STAMINA_SPEED_MODIFIER_KEY,
                -0.2, // 20% speed reduction
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            speedAttribute.addModifier(modifier);
        }
        
        // Apply mining fatigue for slower attacks
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 40, 0, false, false, false));
    }

    private static void removeStaminaEffects(Player player) {
        // Remove speed modifier
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            List<AttributeModifier> modifiersToRemove = speedAttribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(STAMINA_SPEED_MODIFIER_KEY))
                .toList();
            modifiersToRemove.forEach(speedAttribute::removeModifier);
        }
        
        // Remove potion effects
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
    }

    private static void sendStaminaActionBar(Player player, StaminaData data) {
        if (!EMCCOM.getInstance().getConfig().getBoolean("stamina.show_action_bar", true)) {
            return;
        }
        
        double staminaPercent = data.stamina / MAX_STAMINA;
        int barLength = 20;
        int filledBars = (int) (staminaPercent * barLength);
        
        StringBuilder staminaBar = new StringBuilder();
        staminaBar.append("§6Stamina: §r");
        
        // Color based on stamina level
        NamedTextColor barColor;
        if (staminaPercent > 0.6) {
            barColor = NamedTextColor.GREEN;
        } else if (staminaPercent > 0.2) {
            barColor = NamedTextColor.YELLOW;
        } else {
            barColor = NamedTextColor.RED;
        }
        
        // Create the bar
        for (int i = 0; i < barLength; i++) {
            if (i < filledBars) {
                staminaBar.append("§").append(barColor.toString().charAt(1)).append("█");
            } else {
                staminaBar.append("§7█");
            }
        }
        
        staminaBar.append("§r ").append(String.format("%.0f", data.stamina)).append("/").append(String.format("%.0f", MAX_STAMINA));
        
        player.sendActionBar(Component.text(staminaBar.toString()));
    }

    // Task to be run periodically
    public static void updateAllPlayerStamina() {
        for (UUID uuid : playerStamina.keySet()) {
            Player player = EMCCOM.getInstance().getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updatePlayerStamina(player);
            }
        }
    }

    public enum StaminaAction {
        JUMP,
        ATTACK,
        BLOCK
    }
}
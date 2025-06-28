package net.quoky.lava_potions.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler for Decayable Magma Block functionality
 * Tracks attackers for death messages and handles hot floor deaths
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class DecayableMagmaBlockEventHandler {

    // Store custom death messages for players
    private static final Map<UUID, Component> customDeathMessages = new HashMap<>();

    // Track recent attackers for death messages
    private static final Map<UUID, DamageSource> recentAttackers = new HashMap<>();

    /**
     * Track attackers for death messages
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        // Only track if the victim is a player and the attacker is another entity
        if (victim instanceof Player player && source.getEntity() != null) {
            recentAttackers.put(player.getUUID(), source);
        }
    }

    /**
     * Handle death messages for hot floor deaths
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();

        // Check if the death was caused by hot floor damage
        if (entity instanceof Player player && source.is(DamageTypes.HOT_FLOOR)) {
            // Get the custom death message
            Component deathMessage = getDeathMessage(player);

            // Store the custom death message for this player
            customDeathMessages.put(player.getUUID(), deathMessage);

            // Clear the attacker memory for this player
            recentAttackers.remove(player.getUUID());
        }
    }

    /**
     * Get death message for player killed by hot floor
     */
    private static Component getDeathMessage(Player player) {
        DamageSource recentAttacker = recentAttackers.get(player.getUUID());

        if (recentAttacker != null && recentAttacker.getEntity() != null) {
            String attackerName = recentAttacker.getEntity().getName().getString();
            return Component.translatable("death.lava_potions.hot_floor_with_attacker", player.getName(), attackerName);
        } else {
            return Component.translatable("death.lava_potions.hot_floor", player.getName());
        }
    }

    /**
     * Get custom death message for a player
     */
    public static Component getCustomDeathMessage(UUID playerUUID) {
        return customDeathMessages.remove(playerUUID);
    }

    /**
     * Clear all stored death messages
     */
    public static void clearDeathMessages() {
        customDeathMessages.clear();
        recentAttackers.clear();
    }
}
package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that sets attackers on fire when they hit the player
 * Also keeps the player on fire for the effect duration (without damage due to fire resistance)
 * The effect doesn't work when the player is in water and doesn't apply fire in creative mode
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class FlameAuraEffect extends MobEffect {
    public FlameAuraEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xad3c36); // Darker flame red color
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Only apply fire if entity is not already on fire, not in water, and not in creative mode
        if (entity instanceof Player player) {
            if (entity.getRemainingFireTicks() <= 0 && !player.isInWater() && !player.isCreative()) {
                // Set the entity on fire (they have fire resistance so no damage)
                entity.setRemainingFireTicks(40); // 2 seconds (40 ticks)
            }
        } else if (entity.getRemainingFireTicks() <= 0 && !entity.isInWater()) {
            // For non-player entities, just check water
            entity.setRemainingFireTicks(40);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Run this effect every tick for constant checking instead of every second
        return true;
    }

    /**
     * Event handler for when an entity is attacked
     * Sets the attacker on fire if the entity has the Flame Aura effect
     * The effect doesn't work when the player is in water
     * Only applies damage to melee attackers, not projectiles
     */
    @SubscribeEvent
    public static void onEntityAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Check if player has effect and is not in water
            if (player.hasEffect(ModEffects.FLAME_AURA.get()) && !player.isInWater()) {
                Entity attacker = event.getSource().getEntity();

                // Only apply damage if attacker exists and is a living entity
                // This will filter out projectile damage sources and other non-direct attacks
                if (attacker instanceof LivingEntity livingAttacker && event.getSource().isIndirect() == false) {
                    int amplifier = player.getEffect(ModEffects.FLAME_AURA.get()).getAmplifier();

                    // For Tier I (amplifier 0): 2 damage + 4 seconds on fire
                    // For Tier II (amplifier 1): 4 damage + 7 seconds on fire
                    int fireDuration = amplifier == 0 ? 4 : 7;
                    float damage = amplifier == 0 ? 2.0F : 4.0F;

                    // Apply damage to attacker if they are not fire immune
                    if (!livingAttacker.fireImmune()) {
                        // Use thorns damage to provide knockback
                        livingAttacker.hurt(livingAttacker.damageSources().thorns(player), damage);
                    }

                    // Set attacker on fire
                    attacker.setSecondsOnFire(fireDuration);
                }
            }
        }
    }
} 
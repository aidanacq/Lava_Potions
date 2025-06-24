package net.quoky.lava_potions.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Centralized event handler for all skin effects
 * Reduces code duplication and improves performance by handling all skin effects in one place
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class SkinEffectEventHandler {
    
    /**
     * Check if the damage source is explosion-related
     */
    private static boolean isExplosionDamage(DamageSource source) {
        String damageSourceId = source.getMsgId();
        
        // Check for known explosion damage types
        if (source.is(DamageTypes.EXPLOSION) || 
            source.is(DamageTypes.PLAYER_EXPLOSION) ||
            source.is(DamageTypes.BAD_RESPAWN_POINT)) {
            return true;
        }
        
        // Check for explosion-related strings in the damage source ID
        return damageSourceId.contains("explosion") || 
               damageSourceId.contains("blast") ||
               damageSourceId.contains("fireball") ||
               damageSourceId.contains("bomb");
    }
    
    /**
     * Handle damage events for all skin effects
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        
        // Handle Obsidian Skin effect - immunity to explosions
        if (entity.hasEffect(ModEffects.OBSIDIAN_SKIN.get())) {
            if (isExplosionDamage(source)) {
                event.setCanceled(true);
                return;
            }
        }
        
        // Handle Netherite Skin effect - immunity to explosions and fall damage
        if (entity.hasEffect(ModEffects.NETHERITE_SKIN.get())) {
            if (isExplosionDamage(source) || source.is(DamageTypes.FALL)) {
                event.setCanceled(true);
                return;
            }
        }
        
        // Handle Glass Skin effect - amplifies damage received (1.5x damage)
        if (entity.hasEffect(ModEffects.GLASS_SKIN.get())) {
            float originalDamage = event.getAmount();
            event.setAmount(originalDamage * 1.5f);
        }
    }
    
    /**
     * Handle knockback events for skin effects
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Handle Netherite Skin effect - complete knockback immunity
        if (entity.hasEffect(ModEffects.NETHERITE_SKIN.get())) {
            event.setStrength(0.0f);
            return;
        }
        
        // Handle Obsidian Skin effect - 10% knockback reduction
        if (entity.hasEffect(ModEffects.OBSIDIAN_SKIN.get())) {
            float originalStrength = event.getStrength();
            event.setStrength(originalStrength * 0.9f); // Reduce by 10%
        }
    }
} 
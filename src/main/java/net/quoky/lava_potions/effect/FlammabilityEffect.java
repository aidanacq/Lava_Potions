package net.quoky.lava_potions.effect;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that amplifies fire/lava damage taken by 1.5x and extends burn duration by 1.5x
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class FlammabilityEffect extends MobEffect {
    public FlammabilityEffect() {
        super(MobEffectCategory.HARMFUL, 0xe0c122); // Gold/amber color
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Not needed for this effect, handled in events
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // This effect doesn't tick
    }
    
    /**
     * Event handler for fire/lava damage amplification
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(ModEffects.FLAMMABILITY.get())) {
            if (event.getSource().is(DamageTypes.IN_FIRE) || 
                event.getSource().is(DamageTypes.ON_FIRE) || 
                event.getSource().is(DamageTypes.LAVA)) {
                // Amplify fire/lava damage by 1.5x
                event.setAmount(event.getAmount() * 1.5f);
            }
        }
    }
    
    /**
     * Event handler to extend fire duration
     * This will be called when an entity is set on fire
     */
    @SubscribeEvent
    public static void onEntitySetFire(net.minecraftforge.event.entity.EntityTeleportEvent event) {
        if (event.getEntity() instanceof LivingEntity living && living.hasEffect(ModEffects.FLAMMABILITY.get())) {
            // If entity is on fire, extend the duration by 1.5x
            if (living.getRemainingFireTicks() > 0) {
                living.setRemainingFireTicks((int)(living.getRemainingFireTicks() * 1.5f));
            }
        }
    }
} 
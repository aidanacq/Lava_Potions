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
 * Effect that provides immunity to explosions and fall damage
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class NetheriteSkinEffect extends MobEffect {
    public NetheriteSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9b8457); // Tint color
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Not needed for this effect
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // This effect doesn't tick
    }
    
    /**
     * Event handler for fall damage and explosion immunity
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(ModEffects.NETHERITE_SKIN.get())) {
            if (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.FALL)) {
                event.setCanceled(true);
            }
        }
    }
} 
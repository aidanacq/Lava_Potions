package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that amplifies damage received (tier I - 1.5x, tier II - 1.75x)
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class GlassSkinEffect extends MobEffect {
    public GlassSkinEffect() {
        super(MobEffectCategory.HARMFUL, 0xc2f3ff); // Tint color
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Not needed for this effect, handled in event
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // This effect doesn't tick
    }
    
    /**
     * Event handler for damage amplification
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(ModEffects.GLASS_SKIN.get())) {
            int amplifier = event.getEntity().getEffect(ModEffects.GLASS_SKIN.get()).getAmplifier();
            float multiplier = amplifier == 0 ? 1.5f : 1.75f;
            event.setAmount(event.getAmount() * multiplier);
        }
    }
} 
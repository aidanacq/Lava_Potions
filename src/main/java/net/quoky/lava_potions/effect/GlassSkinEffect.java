package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that amplifies damage received (1.5x damage)
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
            event.setAmount(event.getAmount() * 1.5f);
        }
    }
} 
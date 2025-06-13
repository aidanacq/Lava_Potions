package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that provides immunity to explosions
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class ObsidianSkinEffect extends MobEffect {
    public ObsidianSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8e5de3); // Tint color
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
     * Event handler for explosion immunity
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().hasEffect(ModEffects.OBSIDIAN_SKIN.get())) {
            if (event.getSource().is(DamageTypes.EXPLOSION)) {
                event.setCanceled(true);
            }
        }
    }
} 
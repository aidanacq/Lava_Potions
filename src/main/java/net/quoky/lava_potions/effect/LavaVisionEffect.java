package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Effect for Lava Vision potion - allows seeing through lava
 * The actual fog removal is handled by LavaVisionMixin which intercepts
 * isEyeInFluid calls and prevents lava fog rendering when this effect is active
 * This effect serves as a marker for the mixin to detect
 */
public class LavaVisionEffect extends MobEffect {
    public LavaVisionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00ca98);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // This effect doesn't need to tick
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        // No server-side effects needed - all functionality is client-side
    }
}
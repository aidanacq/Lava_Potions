package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Base class for skin effects that provides common functionality
 */
public abstract class BaseSkinEffect extends MobEffect {

    protected BaseSkinEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Not needed for skin effects, handled in events
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // Skin effects don't tick
    }
}
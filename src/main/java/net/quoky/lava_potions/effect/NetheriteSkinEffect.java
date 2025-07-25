package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * Effect that provides immunity to explosions, fall damage, and complete
 * knockback immunity
 */
public class NetheriteSkinEffect extends BaseSkinEffect {
    public NetheriteSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xa47e75); // Tint color
    }
}
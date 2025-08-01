package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * Effect that provides immunity to explosions and 10% knockback reduction
 */
public class ObsidianSkinEffect extends BaseSkinEffect {
    public ObsidianSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8e5de3); // Tint color
    }
}
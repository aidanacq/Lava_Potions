package net.quoky.lava_potions.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Effect that amplifies damage received (1.5x damage)
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID)
public class GlassSkinEffect extends BaseSkinEffect {
    public GlassSkinEffect() {
        super(MobEffectCategory.HARMFUL, 0xc2f3ff); // Tint color
    }
} 
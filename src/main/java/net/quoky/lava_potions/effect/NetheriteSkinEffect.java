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
public class NetheriteSkinEffect extends BaseSkinEffect {
    public NetheriteSkinEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9b8457); // Tint color
    }
} 
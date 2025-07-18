package net.quoky.lava_potions.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.potion.ModPotionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {
    @Inject(method = "getColor(Ljava/util/Collection;)I", at = @At("HEAD"), cancellable = true)
    private static void lavaPotions$overrideColor(Collection<MobEffectInstance> effects, CallbackInfoReturnable<Integer> cir) {
        if (effects == null || effects.isEmpty()) return;
        int colorSumR = 0;
        int colorSumG = 0;
        int colorSumB = 0;
        int count = 0;
        for (MobEffectInstance effect : effects) {
            Potion lavaPotion = ModPotionTypes.getPotionForEffect(effect.getEffect());
            if (lavaPotion != null && ModPotionTypes.isLavaPotion(lavaPotion)) {
                int color = ModPotionTypes.getPotionColor(lavaPotion);
                if (color != -1) {
                    colorSumR += (color >> 16) & 0xFF;
                    colorSumG += (color >> 8) & 0xFF;
                    colorSumB += color & 0xFF;
                    count++;
                }
            }
        }
        if (count > 0) {
            int avgR = colorSumR / count;
            int avgG = colorSumG / count;
            int avgB = colorSumB / count;
            int blended = (avgR << 16) | (avgG << 8) | avgB;
            cir.setReturnValue(blended);
        }
    }
} 
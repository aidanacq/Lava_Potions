package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.tags.FluidTags;
import net.quoky.lava_potions.effect.ModEffects;

/**
 * Mixin to prevent lava fog rendering when player has Lava Vision effect
 * Intercepts Camera.getFluidInCamera to return NONE when checking for lava
 * and the camera entity has the Lava Vision effect, preventing fog rendering
 */
@Mixin(Camera.class)
public class LavaVisionMixin {

    /**
     * Intercept getFluidInCamera to return NONE when the camera entity
     * has the Lava Vision effect and would normally be in lava fog
     */
    @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
    public void onGetFluidInCamera(CallbackInfoReturnable<FogType> cir) {
        // Only modify if the return value is LAVA fog
        if (cir.getReturnValue() != FogType.LAVA) {
            return;
        }

        Camera self = (Camera) (Object) this;
        Entity entity = self.getEntity();

        // Only apply to living entities that can have effects
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        // Check if the entity has the Lava Vision effect
        if (livingEntity.hasEffect(ModEffects.LAVA_VISION.get())) {
            // Return NONE instead of LAVA to prevent lava fog rendering
            cir.setReturnValue(FogType.NONE);
        }
    }
}
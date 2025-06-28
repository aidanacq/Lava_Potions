package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.tags.FluidTags;
import net.quoky.lava_potions.effect.ModEffects;

/**
 * Mixin to make entities with LavaStriderEffect be treated as if they're in
 * water when in lava
 */
@Mixin(Entity.class)
public class LavaStriderMixin {

    /**
         * Intercept isInWater() to return true when the entity has LavaStriderEffect
         * and is in lava
         */
        @Inject(method = "isInWater()Z", at = @At("RETURN"), cancellable = true)
        public void onIsInWater(CallbackInfoReturnable<Boolean> cir) {
            Entity self = (Entity) (Object) this;

            // Only apply to living entities that can have effects
            if (!(self instanceof LivingEntity livingEntity)) {
                return;
            }

            // Check if the entity has the LavaStriderEffect
            if (!livingEntity.hasEffect(ModEffects.LAVA_STRIDER.get())) {
                return;
            }

            // If the entity is in lava, treat it as being in water
            if (self.isInLava()) {
                cir.setReturnValue(true);
            }
        }

    /**
     * Intercept updateFluidHeightAndDoFluidPushing for water fluid when entity
     * has LavaStriderEffect and is in lava
     */
    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"), cancellable = true)
    public void onUpdateFluidHeightAndDoFluidPushing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;

        // Only apply to living entities that can have effects
        if (!(self instanceof LivingEntity livingEntity)) {
            return;
        }

        // Check if the entity has the LavaStriderEffect
        if (!livingEntity.hasEffect(ModEffects.LAVA_STRIDER.get())) {
            return;
        }

        // If the entity is in lava, simulate water fluid behavior
        if (self.isInLava()) {
            // Get the water fluid height calculation
            double waterHeight = self.getFluidHeight(FluidTags.WATER);
            if (waterHeight > 0) {
                // If there's actually water, let the normal method handle it
                return;
            }

            // Simulate being in water by using lava height as water height
            double lavaHeight = self.getFluidHeight(FluidTags.LAVA);
            if (lavaHeight > 0) {
                // Set the return value to true to indicate fluid interaction occurred
                cir.setReturnValue(true);
            }
        }
    }

    /**
        * Intercept getFluidHeight to return lava height when checking for water
        height with LavaStriderEffect
        */
        @Inject(method = "getFluidHeight", at = @At("HEAD"), cancellable = true)
        public void onGetFluidHeight(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid>
        fluidTag, CallbackInfoReturnable<Double> cir) {
            Entity self = (Entity) (Object) this;

            // Only apply to living entities that can have effects
            if (!(self instanceof LivingEntity livingEntity)) {
                return;
            }

            // Check if the entity has the LavaStriderEffect
            if (!livingEntity.hasEffect(ModEffects.LAVA_STRIDER.get())) {
                return;
            }

            // If checking for water height while having LavaStriderEffect, return lava
            // height instead
            if (fluidTag == FluidTags.WATER && self.isInLava()) {
            // Call the original method with lava tag instead
                double lavaHeight =
                self.level().getFluidState(self.blockPosition()).getHeight(self.level(),
                self.blockPosition());
                if (lavaHeight > 0) {
                    cir.setReturnValue(lavaHeight);
                }
            }
        }

    /**
        * Intercept isEyeInFluid to treat lava as water when entity has
        LavaStriderEffect
        */
        @Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
        public void onIsEyeInFluid(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> fluidTag,
        CallbackInfoReturnable<Boolean> cir) {
            Entity self = (Entity) (Object) this;

            // Only apply to living entities that can have effects
            if (!(self instanceof LivingEntity livingEntity)) {
                return;
            }

            // Check if the entity has the LavaStriderEffect
            if (!livingEntity.hasEffect(ModEffects.LAVA_STRIDER.get())) {
                return;
            }

            // If checking for water in eyes while having LavaStriderEffect, check for
            // lava instead
            if (fluidTag == FluidTags.WATER) {
            boolean isEyeInLava = self.isEyeInFluid(FluidTags.LAVA);
                if (isEyeInLava) {
                    cir.setReturnValue(true);
                }
            }
        }
}
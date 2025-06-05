package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.transfer.GenericItemFilling;

import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.fluid.ModFluids;

/**
 * Mixin to ensure our custom awkward lava fluid can fill glass bottles and prioritize our lava handling
 */
@Mixin(value = GenericItemFilling.class, remap = false, priority = 1500)
public class CreateGenericItemFillingMixin {
    
    @Inject(method = "canFillGlassBottleInternally", at = @At("HEAD"), cancellable = true)
    private static void canFillGlassBottleInternally(FluidStack availableFluid, CallbackInfoReturnable<Boolean> cir) {
        try {
            // PRIORITY: Handle vanilla lava fluid first to override AlexsMobs
            if (availableFluid.getFluid() == Fluids.LAVA && availableFluid.getAmount() >= 250) {
                Lava_Potions.LOGGER.debug("Mixin allowing lava fluid to fill glass bottles (priority over other mods)");
                cir.setReturnValue(true);
                return;
            }
            
            // Check if this is our custom awkward lava fluid
            if (availableFluid.getFluid() == ModFluids.AWKWARD_LAVA_POTION_SOURCE.get()) {
                Lava_Potions.LOGGER.debug("Mixin allowing awkward lava fluid to fill glass bottles");
                cir.setReturnValue(true);
                return;
            }
            
            // For all other fluids, let Create handle it normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create GenericItemFilling canFillGlassBottleInternally mixin: {}", e.getMessage());
        }
    }
} 
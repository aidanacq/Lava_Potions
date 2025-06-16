package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.transfer.GenericItemFilling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Mixin to ensure vanilla lava can fill glass bottles and we prioritize our lava handling
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
            
            // Check for our texture metadata in Create potion fluids
            CompoundTag tag = availableFluid.getTag();
            if (tag != null && tag.contains("LavaTextureOverride")) {
                // This is one of our special potion fluids with texture metadata
                Lava_Potions.LOGGER.debug("Mixin detected a Create potion fluid with our texture metadata");
                // Do nothing, let Create handle it normally - we just needed to log this
            }
            
            // For all other fluids, let Create handle it normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create GenericItemFilling canFillGlassBottleInternally mixin: {}", e.getMessage());
        }
    }
} 
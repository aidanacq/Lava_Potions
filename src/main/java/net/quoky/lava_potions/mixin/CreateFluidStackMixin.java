package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Mixin for proper compatibility between our lava potions and other potions
 * in the Create fluid system
 */
@Mixin(value = FluidStack.class, remap = false)
public class CreateFluidStackMixin {
    
    /**
     * Handle special cases for Create potion fluids while allowing
     * both our lava potions and vanilla/modded potions to work
     */
    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    public void onIsEmpty(CallbackInfoReturnable<Boolean> cir) {
        FluidStack self = (FluidStack)(Object)this;
        
        try {
            // Check if it's the base Create potion fluid
            Fluid fluid = self.getFluid();
            ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
            
            if (fluidId != null && "create".equals(fluidId.getNamespace()) &&
                fluidId.getPath().equals("potion")) {
                
                // Always allow our lava potion fluids - they have special texture metadata
                CompoundTag tag = self.getTag();
                if (tag != null && tag.contains("LavaTextureOverride")) {
                    // This is one of our special potion fluids with texture metadata - keep it
                    return;
                }
                
                // Always allow our mod's potions
                if (tag != null && tag.contains("Potion")) {
                    String potionId = tag.getString("Potion");
                    if (potionId.contains("lava_potions:")) {
                        // This is one of our mod's potions - keep it
                        return;
                    }
                }
                
                // IMPORTANT: Allow all other valid potion fluids to work normally
                // Only filter out invalid/empty potion fluids
                if (self.getAmount() <= 0 || (tag == null || !tag.contains("Potion"))) {
                    cir.setReturnValue(true);
                    return;
                }
                
                // Don't filter out other valid potion fluids - we want them to work normally
            }
        } catch (Exception e) {
            // If anything goes wrong, log it but don't break the game
            Lava_Potions.LOGGER.warn("Error in FluidStack isEmpty mixin: {}", e.getMessage());
        }
    }
} 
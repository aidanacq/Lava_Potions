package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.fluid.ModFluids;

/**
 * Mixin to override Create's potion fluid handling for lava bottles
 */
@Mixin(value = PotionFluidHandler.class, remap = false)
public class CreatePotionFluidHandlerMixin {
    
    @Inject(method = "getFluidFromPotionItem", at = @At("HEAD"), cancellable = true)
    private static void getFluidFromPotionItem(ItemStack stack, CallbackInfoReturnable<FluidStack> cir) {
        try {
            Potion potion = PotionUtils.getPotion(stack);
            
            // Check if this is a lava bottle potion
            if (ModPotionTypes.isBaseLavaBottle(potion)) {
                Lava_Potions.LOGGER.info("Mixin intercepted lava bottle - returning lava fluid");
                cir.setReturnValue(new FluidStack(Fluids.LAVA, 250));
                return;
            }
            
            // Check if this is an awkward lava potion
            if (ModPotionTypes.isAwkwardLava(potion)) {
                Lava_Potions.LOGGER.info("Mixin intercepted awkward lava bottle - returning custom lava-textured fluid");
                // Return our custom fluid with lava textures instead of Create's default potion fluid
                cir.setReturnValue(new FluidStack(ModFluids.AWKWARD_LAVA_POTION_SOURCE.get(), 250));
                return;
            }
            
            // For all other potions, let Create handle it normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler mixin: {}", e.getMessage());
        }
    }
} 
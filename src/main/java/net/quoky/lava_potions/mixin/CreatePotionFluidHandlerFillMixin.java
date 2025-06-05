package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.fluid.ModFluids;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to handle filling glass bottles with our custom awkward lava fluid and prioritize our lava bottles
 */
@Mixin(value = PotionFluidHandler.class, remap = false, priority = 1500)
public class CreatePotionFluidHandlerFillMixin {
    
    @Inject(method = "fillBottle", at = @At("HEAD"), cancellable = true)
    private static void fillBottle(ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        try {
            // PRIORITY: Handle vanilla lava fluid first to override AlexsMobs
            if (availableFluid.getFluid() == Fluids.LAVA && availableFluid.getAmount() >= 250) {
                Lava_Potions.LOGGER.info("Mixin intercepted lava fluid filling - creating our lava bottle (priority over other mods)");
                
                // Create our lava bottle
                ItemStack lavaBottle = new ItemStack(Items.POTION);
                PotionUtils.setPotion(lavaBottle, ModPotionTypes.LAVA_BOTTLE.get());
                
                cir.setReturnValue(lavaBottle);
                return;
            }
            
            // Check if this is our custom awkward lava fluid
            if (availableFluid.getFluid() == ModFluids.AWKWARD_LAVA_POTION_SOURCE.get()) {
                Lava_Potions.LOGGER.info("Mixin intercepted awkward lava fluid filling - creating awkward lava bottle");
                
                // Create an awkward lava bottle
                ItemStack awkwardLavaBottle = new ItemStack(Items.POTION);
                PotionUtils.setPotion(awkwardLavaBottle, ModPotionTypes.AWKWARD_LAVA.get());
                
                cir.setReturnValue(awkwardLavaBottle);
                return;
            }
            
            // For all other fluids, let Create handle it normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler fillBottle mixin: {}", e.getMessage());
        }
    }
    
    @Inject(method = "getRequiredAmountForFilledBottle", at = @At("HEAD"), cancellable = true)
    private static void getRequiredAmountForFilledBottle(ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<Integer> cir) {
        try {
            // PRIORITY: Handle vanilla lava fluid first to override AlexsMobs
            if (availableFluid.getFluid() == Fluids.LAVA) {
                // Standard lava bottle amount
                cir.setReturnValue(250);
                return;
            }
            
            // Check if this is our custom awkward lava fluid
            if (availableFluid.getFluid() == ModFluids.AWKWARD_LAVA_POTION_SOURCE.get()) {
                // Standard potion amount
                cir.setReturnValue(250);
                return;
            }
            
            // For all other fluids, let Create handle it normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler getRequiredAmountForFilledBottle mixin: {}", e.getMessage());
        }
    }
} 
package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to handle filling glass bottles with vanilla lava to create our lava bottle (prioritized over other mods)
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
            
            // For Create potion fluids, check for our texture metadata
            CompoundTag tag = availableFluid.getTag();
            if (tag != null && tag.contains("LavaTextureOverride")) {
                // This is one of our special potion fluids with texture metadata
                Lava_Potions.LOGGER.debug("Processing Create potion fluid with our texture metadata");
                // Do nothing, let Create handle it normally - we just needed to log this
            }
            
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
            
            // For all other fluids, let Create handle it normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler getRequiredAmountForFilledBottle mixin: {}", e.getMessage());
        }
    }
} 
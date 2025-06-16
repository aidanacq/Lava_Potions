package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to handle special cases for lava potions in Create's fluid system
 */
@Mixin(value = PotionFluidHandler.class, remap = false)
public class CreatePotionFluidHandlerMixin {
    
    /**
     * Handle base lava bottles specially - return actual lava fluid instead of potion fluid
     */
    @Inject(method = "getFluidFromPotionItem", at = @At("HEAD"), cancellable = true)
    private static void getFluidFromPotionItem(ItemStack stack, CallbackInfoReturnable<FluidStack> cir) {
        try {
            Potion potion = PotionUtils.getPotion(stack);
            
            // Check if this is a lava bottle potion - return actual lava
            if (ModPotionTypes.isBaseLavaBottle(potion)) {
                Lava_Potions.LOGGER.debug("Mixin intercepted lava bottle - returning lava fluid");
                cir.setReturnValue(new FluidStack(Fluids.LAVA, 250));
                return;
            }
            
            // Let Create handle all other cases, we'll add texture metadata later
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Add texture metadata to the fluid stack for awkward lava
     */
    @Inject(method = "getFluidFromPotionItem", 
            at = @At("RETURN"),
            cancellable = true)
    private static void addTextureMetadataToFluid(ItemStack stack, CallbackInfoReturnable<FluidStack> cir) {
        try {
            FluidStack fluidStack = cir.getReturnValue();
            if (fluidStack == null || fluidStack.isEmpty()) {
                return;
            }
            
            Potion potion = PotionUtils.getPotion(stack);
            
            // Add texture metadata to awkward lava potion
            if (ModPotionTypes.isAwkwardLava(potion)) {
                CompoundTag tag = fluidStack.getOrCreateTag();
                tag.putString("LavaTextureOverride", "minecraft:block/lava_still");
                tag.putString("LavaFlowingTextureOverride", "minecraft:block/lava_flow");
                Lava_Potions.LOGGER.debug("Added lava texture metadata to awkward lava potion fluid");
            }
            // Removed Flame Aura specific handling - let it be handled by CreatePotionFluidMixin like other effect potions
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error adding texture metadata in PotionFluidHandler mixin: {}", e.getMessage());
        }
    }
} 
package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Consolidated mixin to handle all Create fluid interactions for lava potions
 * Merges functionality from multiple smaller mixins for better maintainability
 */
@Mixin(value = PotionFluidHandler.class, remap = false, priority = 1500)
public class CreatePotionFluidHandlerMixin {
    
    // Mixin constants
    private static final int LAVA_BOTTLE_AMOUNT = 250;
    private static final String LAVA_TEXTURE_OVERRIDE_TAG = "LavaTextureOverride";
    
    /**
     * Prevent Create from recognizing uncraftable lava potions as valid potion items
     * This stops the auto-generation of emptying recipes for them
     */
    @Inject(method = "isPotionItem", at = @At("HEAD"), cancellable = true)
    private static void preventUncraftableLavaPotionRecognition(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                String potionId = tag.getString("Potion");
                
                if (potionId.contains("lava_potions:")) {
                    Potion actualPotion = PotionUtils.getPotion(stack);
                    ResourceLocation expectedPotionRL = ResourceLocation.tryParse(potionId);
                    
                    if (expectedPotionRL != null) {
                        Potion expectedPotion = ForgeRegistries.POTIONS.getValue(expectedPotionRL);
                        
                        if (expectedPotion != null && actualPotion != expectedPotion) {
                            Lava_Potions.LOGGER.debug("Preventing Create from recognizing uncraftable lava potion as potion item: {}", potionId);
                            cir.setReturnValue(false);
                            return;
                        }
                        
                        if (actualPotion == null || actualPotion == Potions.EMPTY) {
                            Lava_Potions.LOGGER.debug("Preventing Create from recognizing empty lava potion as potion item: {}", potionId);
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler isPotionItem mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Handle base lava bottles specially - return actual lava fluid instead of potion fluid
     * Also prevent processing of uncraftable/empty potions that might reference our lava potions
     */
    @Inject(method = "getFluidFromPotionItem", at = @At("HEAD"), cancellable = true)
    private static void getFluidFromPotionItem(ItemStack stack, CallbackInfoReturnable<FluidStack> cir) {
        try {
            Potion potion = PotionUtils.getPotion(stack);
            
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                String potionId = tag.getString("Potion");
                
                if (potionId.contains("lava_potions:")) {
                    ResourceLocation expectedPotionRL = ResourceLocation.tryParse(potionId);
                    if (expectedPotionRL != null) {
                        Potion expectedPotion = ForgeRegistries.POTIONS.getValue(expectedPotionRL);
                        
                        if (expectedPotion != null && potion != expectedPotion) {
                            Lava_Potions.LOGGER.debug("Blocked uncraftable potion with mismatched NBT: NBT says '{}' but potion type is '{}'", 
                                potionId, ForgeRegistries.POTIONS.getKey(potion));
                            cir.setReturnValue(FluidStack.EMPTY);
                            return;
                        }
                        
                        if (potion == null || potion == Potions.EMPTY) {
                            Lava_Potions.LOGGER.debug("Blocked uncraftable potion with lava NBT but empty potion type: {}", potionId);
                            cir.setReturnValue(FluidStack.EMPTY);
                            return;
                        }
                    }
                }
            }
            
            if (potion == null || potion == Potions.EMPTY) {
                return;
            }
            
            if (ModPotionTypes.isBaseLavaBottle(potion)) {
                Lava_Potions.LOGGER.debug("Mixin intercepted lava bottle - returning lava fluid");
                cir.setReturnValue(new FluidStack(Fluids.LAVA, LAVA_BOTTLE_AMOUNT));
                return;
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler mixin: {}", e.getMessage());
        }
    }

    /**
     * Handle filling glass bottles with vanilla lava to create our lava bottle (prioritized over other mods)
     */
    @Inject(method = "fillBottle", at = @At("HEAD"), cancellable = true)
    private static void fillBottle(ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        try {
            if (availableFluid.getFluid() == Fluids.LAVA && availableFluid.getAmount() >= LAVA_BOTTLE_AMOUNT) {
                Lava_Potions.LOGGER.debug("Mixin intercepted lava fluid filling - creating our lava bottle (priority over other mods)");
                
                ItemStack lavaBottle = new ItemStack(Items.POTION);
                PotionUtils.setPotion(lavaBottle, ModPotionTypes.LAVA_BOTTLE.get());
                
                cir.setReturnValue(lavaBottle);
                return;
            }
            
            CompoundTag tag = availableFluid.getTag();
            if (tag != null && tag.contains(LAVA_TEXTURE_OVERRIDE_TAG)) {
                Lava_Potions.LOGGER.debug("Processing Create potion fluid with our texture metadata");
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler fillBottle mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Set required amount for filling bottles with lava
     */
    @Inject(method = "getRequiredAmountForFilledBottle", at = @At("HEAD"), cancellable = true)
    private static void getRequiredAmountForFilledBottle(ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<Integer> cir) {
        try {
            if (availableFluid.getFluid() == Fluids.LAVA) {
                cir.setReturnValue(LAVA_BOTTLE_AMOUNT);
                return;
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler getRequiredAmountForFilledBottle mixin: {}", e.getMessage());
        }
    }
}

/**
 * Mixin for GenericItemFilling to ensure vanilla lava can fill glass bottles
 */
@Mixin(value = GenericItemFilling.class, remap = false, priority = 1500)
class CreateGenericItemFillingMixin {
    
    @Inject(method = "canFillGlassBottleInternally", at = @At("HEAD"), cancellable = true)
    private static void canFillGlassBottleInternally(FluidStack availableFluid, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (availableFluid.getFluid() == Fluids.LAVA && availableFluid.getAmount() >= 250) {
                Lava_Potions.LOGGER.debug("Mixin allowing lava fluid to fill glass bottles (priority over other mods)");
                cir.setReturnValue(true);
                return;
            }
            
            CompoundTag tag = availableFluid.getTag();
            if (tag != null && tag.contains("LavaTextureOverride")) {
                Lava_Potions.LOGGER.debug("Mixin detected a Create potion fluid with our texture metadata");
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create GenericItemFilling canFillGlassBottleInternally mixin: {}", e.getMessage());
        }
    }
} 
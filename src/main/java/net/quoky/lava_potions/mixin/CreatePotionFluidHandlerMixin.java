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
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to handle special cases for lava potions in Create's fluid system
 */
@Mixin(value = PotionFluidHandler.class, remap = false)
public class CreatePotionFluidHandlerMixin {
    
    /**
     * Prevent Create from recognizing uncraftable lava potions as valid potion items
     * This stops the auto-generation of emptying recipes for them
     */
    @Inject(method = "isPotionItem", at = @At("HEAD"), cancellable = true)
    private static void preventUncraftableLavaPotionRecognition(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            // Check if this has lava potion NBT but is actually uncraftable
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                String potionId = tag.getString("Potion");
                
                if (potionId.contains("lava_potions:")) {
                    Potion actualPotion = PotionUtils.getPotion(stack);
                    ResourceLocation expectedPotionRL = ResourceLocation.tryParse(potionId);
                    
                    if (expectedPotionRL != null) {
                        Potion expectedPotion = ForgeRegistries.POTIONS.getValue(expectedPotionRL);
                        
                        // If NBT doesn't match actual potion type, it's uncraftable - don't treat as potion item
                        if (expectedPotion != null && actualPotion != expectedPotion) {
                            Lava_Potions.LOGGER.warn("Preventing Create from recognizing uncraftable lava potion as potion item: {}", potionId);
                            cir.setReturnValue(false);
                            return;
                        }
                        
                        // If NBT says lava potion but actual type is empty, it's uncraftable
                        if (actualPotion == null || actualPotion == Potions.EMPTY) {
                            Lava_Potions.LOGGER.warn("Preventing Create from recognizing empty lava potion as potion item: {}", potionId);
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
            
            // Let Create handle normal cases
            
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
            
            // AGGRESSIVE BLOCKING: Block any potion that has lava potion NBT but isn't a proper potion type
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                String potionId = tag.getString("Potion");
                
                // If NBT references our lava potions but the actual potion type is wrong, block it
                if (potionId.contains("lava_potions:")) {
                    // Check if the actual potion type matches the NBT
                    ResourceLocation expectedPotionRL = ResourceLocation.tryParse(potionId);
                    if (expectedPotionRL != null) {
                        Potion expectedPotion = ForgeRegistries.POTIONS.getValue(expectedPotionRL);
                        
                        // If the NBT says it's a lava potion but the actual potion type doesn't match, it's uncraftable
                        if (expectedPotion != null && potion != expectedPotion) {
                            Lava_Potions.LOGGER.warn("Blocked uncraftable potion with mismatched NBT: NBT says '{}' but potion type is '{}'", 
                                potionId, ForgeRegistries.POTIONS.getKey(potion));
                            cir.setReturnValue(FluidStack.EMPTY);
                            return;
                        }
                        
                        // If NBT says lava potion but potion type is null/empty, it's definitely uncraftable
                        if (potion == null || potion == Potions.EMPTY) {
                            Lava_Potions.LOGGER.warn("Blocked uncraftable potion with lava NBT but empty potion type: {}", potionId);
                            cir.setReturnValue(FluidStack.EMPTY);
                            return;
                        }
                    }
                }
            }
            
            // Block completely empty potions that somehow got here
            if (potion == null || potion == Potions.EMPTY) {
                // Let Create handle normal empty potions (like water bottles)
                return;
            }
            
            // Handle our specific lava bottle potion - return actual lava fluid instead of potion fluid
            if (ModPotionTypes.isBaseLavaBottle(potion)) {
                Lava_Potions.LOGGER.debug("Mixin intercepted lava bottle - returning lava fluid");
                cir.setReturnValue(new FluidStack(Fluids.LAVA, 250));
                return;
            }
            
            // Let Create handle all other cases normally
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluidHandler mixin: {}", e.getMessage());
        }
    }

} 
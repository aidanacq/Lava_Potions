package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.fluid.ModFluids;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to redirect awkward lava potions to use our custom fluid instead of Create's default potion fluid.
 * This ensures the awkward lava potion gets the custom ID 'create:potion/lava_potions/awkward_lava'
 */
@Mixin(value = PotionFluid.class, remap = false)
public class CreatePotionFluidMixin {
    
    /**
     * Intercepts the of() method to redirect awkward lava potions to our custom fluid
     */
    @Inject(method = "of", 
            at = @At("HEAD"), 
            cancellable = true)
    private static void redirectAwkwardLavaToCustomFluid(int amount, Potion potion, BottleType bottleType, 
                                                        CallbackInfoReturnable<FluidStack> cir) {
        try {
            // Only redirect awkward lava potion to our custom fluid
            if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                FluidStack customFluid = ModFluids.LavaPotionFluid.of(amount, potion, bottleType);
                cir.setReturnValue(customFluid);
                Lava_Potions.LOGGER.debug("Redirected awkward lava potion to custom fluid: create:potion/lava_potions/awkward_lava");
                return;
            }
            
            // For all other lava potions, let Create handle them normally but add texture metadata
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluid redirect mixin: {}", e.getMessage());
        }
    }
    
    /**
     * Add texture metadata to remaining lava potions that use Create's standard potion fluid
     */
    @Inject(method = "of", 
            at = @At("RETURN"))
    private static void addTextureMetadataToOtherLavaPotions(int amount, Potion potion, BottleType bottleType, 
                                                            CallbackInfoReturnable<FluidStack> cir) {
        try {
            FluidStack fluidStack = cir.getReturnValue();
            
            // Skip if the fluid stack is empty (shouldn't happen)
            if (fluidStack.isEmpty()) {
                return;
            }
            
            // Skip awkward lava since it's handled by the custom fluid
            if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                return;
            }
            
            CompoundTag tag = fluidStack.getOrCreateTag();
            
            // For base lava potion - use vanilla lava texture
            if (ModPotionTypes.isBaseLavaBottle(potion)) {
                tag.putString("LavaTextureOverride", "minecraft:block/lava_still");
                tag.putString("LavaFlowingTextureOverride", "minecraft:block/lava_flow");
                tag.putBoolean("BehaveLikeLava", true);
                tag.putBoolean("PreventPlacement", true);
                
                Lava_Potions.LOGGER.debug("Adding lava texture and behavior metadata to Create potion fluid for base lava potion");
            }
            // For all effect lava potions - use gray lava texture with tinting
            else if (ModPotionTypes.isEffectLavaPotion(potion)) {
                tag.putString("LavaTextureOverride", "lava_potions:block/gray_lava_still");
                tag.putString("LavaFlowingTextureOverride", "lava_potions:block/gray_lava_flow");
                
                Lava_Potions.LOGGER.debug("Adding gray lava texture metadata to Create potion fluid for effect potion: "
                    + ForgeRegistries.POTIONS.getKey(potion));
            }
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluid texture mixin: {}", e.getMessage());
        }
    }
} 
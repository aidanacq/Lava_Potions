package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to add texture metadata to Create's potion fluid system for lava potions.
 * Instead of replacing the fluid entirely, we now just add texture override metadata.
 * 
 * For Awkward Lava: Uses the vanilla lava texture (pure lava look)
 * For Effect Lava Potions: Uses a gray lava texture that will be tinted by the color handler
 */
@Mixin(value = PotionFluid.class, remap = false)
public class CreatePotionFluidMixin {
    
    /**
     * Intercepts the of() method to add custom texture metadata 
     * to Create's potion fluid when it's a lava potion
     */
    @Inject(method = "of", 
            at = @At("RETURN"))
    private static void interceptPotionFluidCreation(int amount, Potion potion, BottleType bottleType, 
                                                    CallbackInfoReturnable<FluidStack> cir) {
        try {
            FluidStack fluidStack = cir.getReturnValue();
            
            // Skip if the fluid stack is empty (shouldn't happen)
            if (fluidStack.isEmpty()) {
                return;
            }
            
            // For awkward lava or base lava potion - use vanilla lava texture
            if (potion == ModPotionTypes.AWKWARD_LAVA.get() || ModPotionTypes.isBaseLavaBottle(potion)) {
                // Add metadata to use lava texture instead of default potion texture
                CompoundTag tag = fluidStack.getOrCreateTag();
                tag.putString("LavaTextureOverride", "minecraft:block/lava_still");
                tag.putString("LavaFlowingTextureOverride", "minecraft:block/lava_flow");
                
                Lava_Potions.LOGGER.debug("Adding lava texture metadata to Create potion fluid for base lava potion");
            }
            // For all effect lava potions - use gray lava texture with tinting
            else if (ModPotionTypes.isEffectLavaPotion(potion)) {
                CompoundTag tag = fluidStack.getOrCreateTag();
                tag.putString("LavaTextureOverride", "lava_potions:block/gray_lava_still");
                tag.putString("LavaFlowingTextureOverride", "lava_potions:block/gray_lava_flow");
                
                Lava_Potions.LOGGER.debug("Adding gray lava texture metadata to Create potion fluid for effect potion: "
                    + ForgeRegistries.POTIONS.getKey(potion));
            }
            
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluid mixin: {}", e.getMessage());
        }
    }
} 
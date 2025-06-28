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
 * Consolidated mixin for Create potion fluid handling
 * Handles fluid redirection and texture metadata for lava potions
 */
@Mixin(value = PotionFluid.class, remap = false)
public class CreatePotionFluidMixin {

    // Mixin constants
    private static final String LAVA_TEXTURE_OVERRIDE_TAG = "LavaTextureOverride";
    private static final String LAVA_FLOWING_TEXTURE_OVERRIDE_TAG = "LavaFlowingTextureOverride";

    /**
     * Intercepts the of() method to redirect awkward lava potions to our custom
     * fluid
     */
    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void redirectAwkwardLavaToCustomFluid(int amount, Potion potion, BottleType bottleType,
            CallbackInfoReturnable<FluidStack> cir) {
        try {
            if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                // Ensure our custom fluid is available before trying to use it
                if (ModFluids.LAVA_POTION_SOURCE.get() != null) {
                    FluidStack customFluid = ModFluids.LavaPotionFluid.of(amount, potion, bottleType);
                    if (customFluid != null && !customFluid.isEmpty()) {
                        cir.setReturnValue(customFluid);
                        Lava_Potions.LOGGER.debug(
                                "Redirected awkward lava potion to custom fluid: create:potion/lava_potions/awkward_lava");
                        return;
                    }
                }
                Lava_Potions.LOGGER.warn("Custom fluid not available, falling back to default Create potion fluid");
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluid redirect mixin: {}", e.getMessage());
        }
    }

    /**
     * Add texture metadata to remaining lava potions that use Create's standard
     * potion fluid
     */
    @Inject(method = "of", at = @At("RETURN"))
    private static void addTextureMetadataToOtherLavaPotions(int amount, Potion potion, BottleType bottleType,
            CallbackInfoReturnable<FluidStack> cir) {
        try {
            FluidStack fluidStack = cir.getReturnValue();

            if (fluidStack.isEmpty() || potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                return;
            }

            CompoundTag tag = fluidStack.getOrCreateTag();

            if (ModPotionTypes.isBaseLavaBottle(potion)) {
                tag.putString(LAVA_TEXTURE_OVERRIDE_TAG, "minecraft:block/lava_still");
                tag.putString(LAVA_FLOWING_TEXTURE_OVERRIDE_TAG, "minecraft:block/lava_flow");
                tag.putBoolean("BehaveLikeLava", true);
                tag.putBoolean("PreventPlacement", true);

                Lava_Potions.LOGGER
                        .debug("Adding lava texture and behavior metadata to Create potion fluid for base lava potion");
            } else if (ModPotionTypes.isEffectLavaPotion(potion)) {
                tag.putString(LAVA_TEXTURE_OVERRIDE_TAG, "lava_potions:block/gray_lava_still");
                tag.putString(LAVA_FLOWING_TEXTURE_OVERRIDE_TAG, "lava_potions:block/gray_lava_flow");

                Lava_Potions.LOGGER.debug("Adding gray lava texture metadata to Create potion fluid for effect potion: "
                        + ForgeRegistries.POTIONS.getKey(potion));
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in Create PotionFluid texture mixin: {}", e.getMessage());
        }
    }
}

/**
 * Mixin for proper compatibility between our lava potions and other potions
 * in the Create fluid system
 */
@Mixin(value = FluidStack.class, remap = false)
class CreateFluidStackMixin {

    /**
     * Handle special cases for Create potion fluids while allowing
     * both our lava potions and vanilla/modded potions to work
     */
    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    public void onIsEmpty(CallbackInfoReturnable<Boolean> cir) {
        FluidStack self = (FluidStack) (Object) this;

        try {
            ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(self.getFluid());

            if (fluidId != null && "create".equals(fluidId.getNamespace()) &&
                    fluidId.getPath().equals("potion")) {

                CompoundTag tag = self.getTag();
                if (tag != null && tag.contains("LavaTextureOverride")) {
                    return;
                }

                if (tag != null && tag.contains("Potion")) {
                    String potionId = tag.getString("Potion");
                    if (potionId.contains("lava_potions:")) {
                        return;
                    }
                }

                if (self.getAmount() <= 0 || (tag == null || !tag.contains("Potion"))) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in FluidStack isEmpty mixin: {}", e.getMessage());
        }
    }
}
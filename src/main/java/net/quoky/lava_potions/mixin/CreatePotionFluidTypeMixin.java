package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllFluids.TintedFluidType;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Mixin to override the fluid texture for lava potions in Create's potion fluid system
 */
@Mixin(value = TintedFluidType.class, remap = false)
public abstract class CreatePotionFluidTypeMixin {

    // Exact colors used in item textures - matches colors in LavaPotionColorHandler
    private static final int OBSIDIAN_SKIN_COLOR = 0x8e5de3;   // Purple
    private static final int NETHERITE_SKIN_COLOR = 0xa47e75;  // Light brown
    private static final int GLASS_SKIN_COLOR = 0xc2f3ff;      // Light blue
    private static final int HEAT_COLOR = 0xf7a236;      // Red
    private static final int FLAMMABILITY_COLOR = 0xffec99;    // Gold/amber
    private static final int PYROMANCY_COLOR = 0xe5291f;     // Orange
    private static final int MAGMA_WALKER_COLOR = 0xd05c00;    // Orange

    /**
     * Injects into the client initialization method to override textures for lava potions
     * but only applies to PotionFluidType instances
     */
    @Inject(method = "initializeClient",
            at = @At("HEAD"),
            cancellable = true)
    public void onInitializeClient(java.util.function.Consumer<IClientFluidTypeExtensions> consumer, CallbackInfo ci) {
        try {
            // Only apply to PotionFluidType instances
            if (!((Object)this instanceof PotionFluidType)) {
                return;
            }
            
            // Use Create's potion textures as default
            ResourceLocation defaultStillTexture = ResourceLocation.fromNamespaceAndPath("create", "fluid/potion_still");
            ResourceLocation defaultFlowingTexture = ResourceLocation.fromNamespaceAndPath("create", "fluid/potion_flow");
            
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return defaultStillTexture;
                }
                
                @Override
                public ResourceLocation getFlowingTexture() {
                    return defaultFlowingTexture;
                }
                
                @Override
                public ResourceLocation getStillTexture(FluidStack stack) {
                    if (stack != null && stack.hasTag()) {
                        CompoundTag tag = stack.getTag();
                        String potionId = tag.getString("Potion");
                        
                        if (potionId.contains("lava_potions:")) {
                            Potion potion = ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(potionId));
                            if (potion != null) {
                                // Awkward lava gets vanilla lava texture
                                if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                                    return ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_still");
                                }
                                // All other lava potions (effect potions) get our gray lava texture
                                else if (ModPotionTypes.isEffectLavaPotion(potion)) {
                                    return ResourceLocation.fromNamespaceAndPath("lava_potions", "block/gray_lava_still");
                                }
                            }
                        }
                    }
                    return getStillTexture(); // Fallback to default
                }
                
                @Override
                public ResourceLocation getFlowingTexture(FluidStack stack) {
                    if (stack != null && stack.hasTag()) {
                        CompoundTag tag = stack.getTag();
                        String potionId = tag.getString("Potion");
                        
                        if (potionId.contains("lava_potions:")) {
                            Potion potion = ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(potionId));
                            if (potion != null) {
                                // Awkward lava gets vanilla lava texture
                                if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                                    return ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_flow");
                                }
                                // All other lava potions (effect potions) get our gray lava texture
                                else if (ModPotionTypes.isEffectLavaPotion(potion)) {
                                    return ResourceLocation.fromNamespaceAndPath("lava_potions", "block/gray_lava_flow");
                                }
                            }
                        }
                    }
                    return getFlowingTexture(); // Fallback to default
                }
                
                @Override
                public int getTintColor(FluidStack stack) {
                    if (stack == null || !stack.hasTag()) {
                        return 0xFFFFFFFF; // Default white for empty stacks
                    }
                    
                    CompoundTag tag = stack.getTag();
                    String potionId = tag.getString("Potion");
                    
                    if (potionId.isEmpty()) {
                        return 0xFFFFFFFF;
                    }
                    
                    // Check if it's one of our lava potions
                    if (potionId.contains("lava_potions:")) {
                        Potion potion = ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(potionId));
                        if (potion != null) {
                            // Base lava potions have no tint
                            if (potion == ModPotionTypes.LAVA_BOTTLE.get() ||
                                potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                                return 0xFFFFFFFF; // Pure white - no tint
                            }
                            
                            // For effect lava potions, use our pre-defined exact colors as tints
                            if (potion == ModPotionTypes.OBSIDIAN_SKIN.get() || 
                                potion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                                return OBSIDIAN_SKIN_COLOR | 0xff000000;
                            }
                            if (potion == ModPotionTypes.NETHERITE_SKIN.get() || 
                                potion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                                return NETHERITE_SKIN_COLOR | 0xff000000; 
                            }
                            if (potion == ModPotionTypes.GLASS_SKIN.get() || 
                                potion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                                return GLASS_SKIN_COLOR | 0xff000000;
                            }
                            if (potion == ModPotionTypes.HEAT.get() ||
                                potion == ModPotionTypes.HEAT_LONG.get() ||
                                potion == ModPotionTypes.HEAT_STRONG.get()) {
                                return HEAT_COLOR | 0xff000000;
                            }
                            if (potion == ModPotionTypes.FLAMMABILITY.get() ||
                                potion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                                return FLAMMABILITY_COLOR | 0xff000000;
                            }
                            if (potion == ModPotionTypes.PYROMANCY.get() ||
                                potion == ModPotionTypes.PYROMANCY_LONG.get() ||
                                potion == ModPotionTypes.PYROMANCY_STRONG.get()) {
                                return PYROMANCY_COLOR | 0xff000000;
                            }
                            if (potion == ModPotionTypes.MAGMA_WALKER.get() ||
                                potion == ModPotionTypes.MAGMA_WALKER_LONG.get()) {
                                return MAGMA_WALKER_COLOR | 0xff000000;
                            }
                            if (potion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
                                return MAGMA_WALKER_COLOR | 0xff000000;
                            }
                        }
                    } else {
                        // Non-lava potion: use Create's default behavior
                        return PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
                    }
                    
                    // Default white if no match
                    return 0xFFFFFFFF;
                }
            });
            
            // Cancel the original initialization since we're handling everything
            ci.cancel();
        } catch (Exception e) {
            Lava_Potions.LOGGER.warn("Error in PotionFluidType mixin: {}", e.getMessage());
            // Don't cancel if there's an error - let the original method run
        }
    }
} 
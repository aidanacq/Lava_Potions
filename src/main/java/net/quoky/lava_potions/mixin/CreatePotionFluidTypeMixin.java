package net.quoky.lava_potions.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllFluids.TintedFluidType;
import com.simibubi.create.content.fluids.potion.PotionFluid.PotionFluidType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;

import java.util.List;

/**
 * Mixin to override the fluid texture for lava potions in Create's system
 * but preserve the normal coloring for regular potions
 */
@Mixin(value = TintedFluidType.class, remap = false)
public abstract class CreatePotionFluidTypeMixin {

    // Exact colors used in item textures - matches colors in LavaPotionColorHandler
    private static final int OBSIDIAN_SKIN_COLOR = 0x8e5de3;   // Purple
    private static final int NETHERITE_SKIN_COLOR = 0x9b8457;  // Light brown
    private static final int GLASS_SKIN_COLOR = 0xc2f3ff;      // Light blue
    private static final int FLAME_AURA_COLOR = 0xad3c36;      // Red
    private static final int FLAMMABILITY_COLOR = 0xe0c122;    // Gold/amber

    /**
     * Injects into the client initialization method to override textures for lava potions
     * but keeps the original behavior for regular potions
     */
    @Inject(method = "initializeClient",
            at = @At("HEAD"),
            cancellable = true)
    public void onInitializeClient(java.util.function.Consumer<IClientFluidTypeExtensions> consumer, CallbackInfo ci) {
        // Only apply to PotionFluidType instances
        if (!((Object)this instanceof PotionFluidType))
            return;
            
        // Use the default Minecraft water textures as base (same as Create does)
        ResourceLocation defaultStillTexture = new ResourceLocation("minecraft", "block/water_still");
        ResourceLocation defaultFlowingTexture = new ResourceLocation("minecraft", "block/water_flow");
        
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
                    if (tag.contains("LavaTextureOverride")) {
                        String texturePath = tag.getString("LavaTextureOverride");
                        Lava_Potions.LOGGER.debug("Using custom still texture: {}", texturePath);
                        return ResourceLocation.tryParse(texturePath);
                    }
                }
                return getStillTexture(); // Fallback to default
            }
            
            @Override
            public ResourceLocation getFlowingTexture(FluidStack stack) {
                if (stack != null && stack.hasTag()) {
                    CompoundTag tag = stack.getTag();
                    if (tag.contains("LavaFlowingTextureOverride")) {
                        String texturePath = tag.getString("LavaFlowingTextureOverride");
                        Lava_Potions.LOGGER.debug("Using custom flowing texture: {}", texturePath);
                        return ResourceLocation.tryParse(texturePath);
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
                
                // Get the potion from the tag
                String potionId = tag.getString("Potion");
                
                // Skip early if there's no potion
                if (potionId.isEmpty()) {
                    return 0xFFFFFFFF;
                }
                
                // Get the potion
                Potion potion = ForgeRegistries.POTIONS.getValue(ResourceLocation.tryParse(potionId));
                if (potion == null) {
                    return 0xFFFFFFFF;
                }
                
                // Get the potion registry name
                ResourceLocation potionRL = ForgeRegistries.POTIONS.getKey(potion);
                if (potionRL == null) {
                    return 0xFFFFFFFF;
                }
                
                // Check if it's one of our lava potions
                if (potionRL.getNamespace().equals("lava_potions")) {
                    // Base lava potions have no tint
                    if (potion == ModPotionTypes.LAVA_BOTTLE.get() || 
                        potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                        return 0xFFFFFFFF; // Pure white - no tint
                    }
                    
                    // For effect lava potions, use our pre-defined exact colors
                    // instead of calculating from effects (which can combine and change)
                    Lava_Potions.LOGGER.debug("Using exact item texture color for lava potion: {}", potionId);
                    
                    // Match by potion type & return exact color from item texture
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
                    if (potion == ModPotionTypes.FLAME_AURA.get() || 
                        potion == ModPotionTypes.FLAME_AURA_LONG.get() || 
                        potion == ModPotionTypes.FLAME_AURA_STRONG.get()) {
                        return FLAME_AURA_COLOR | 0xff000000;
                    } 
                    if (potion == ModPotionTypes.FLAMMABILITY.get() || 
                        potion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                        return FLAMMABILITY_COLOR | 0xff000000;
                    }
                } else {
                    // Non-lava potion: use Create's default behavior
                    // (calculating color from all potion effects)
                    return PotionUtils.getColor(PotionUtils.getAllEffects(tag)) | 0xff000000;
                }
                
                // Default white if no match (shouldn't happen)
                return 0xFFFFFFFF;
            }
        });
        
        // Cancel the original initialization since we're handling everything
        ci.cancel();
    }
} 
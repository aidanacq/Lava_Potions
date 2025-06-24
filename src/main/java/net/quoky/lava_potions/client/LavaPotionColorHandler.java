package net.quoky.lava_potions.client;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;

/**
 * Handles color tinting for lava potions
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LavaPotionColorHandler {
        
    // Effect colors from effect classes
    private static final int OBSIDIAN_SKIN_COLOR = 0x8e5de3; // From ObsidianSkinEffect
    private static final int NETHERITE_SKIN_COLOR = 0xa47e75; // From NetheriteSkinEffect
    private static final int GLASS_SKIN_COLOR = 0xc2f3ff; // From GlassSkinEffect
    private static final int HEAT_COLOR = 0xf7a236; // From HeatEffect
    private static final int FLAMMABILITY_COLOR = 0xffec99; // From FlammabilityEffect
    private static final int PYROMANCY_COLOR = 0xe5291f; // From PyromancyEffect
    private static final int MAGMA_WALKER_COLOR = 0xd05c00; // From MagmaWalkerEffect

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register color handler for vanilla potion items
        event.register((stack, tintIndex) -> {
            // Handle lava potions
            if (BrewingRecipes.isVanillaPotionWithLavaType(stack)) {
                Potion potion = PotionUtils.getPotion(stack);
                
                // Only apply tint to the liquid layer (layer0)
                if (tintIndex == 0) {
                    // All lava potions use lava_contents.png with appropriate tinting
                    if (potion == ModPotionTypes.LAVA_BOTTLE.get() || potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                        return 0xFFFFFF; // No tint for base potions
                    } else if (potion == ModPotionTypes.OBSIDIAN_SKIN.get() || potion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                        return OBSIDIAN_SKIN_COLOR;
                    } else if (potion == ModPotionTypes.NETHERITE_SKIN.get() || potion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                        return NETHERITE_SKIN_COLOR;
                    } else if (potion == ModPotionTypes.GLASS_SKIN.get() || potion == ModPotionTypes.GLASS_SKIN_LONG.get()) {
                        return GLASS_SKIN_COLOR;
                    } else if (potion == ModPotionTypes.HEAT.get() || potion == ModPotionTypes.HEAT_LONG.get() ||
                                potion == ModPotionTypes.HEAT_STRONG.get()) {
                        return HEAT_COLOR;
                    } else if (potion == ModPotionTypes.FLAMMABILITY.get() || potion == ModPotionTypes.FLAMMABILITY_LONG.get()) {
                        return FLAMMABILITY_COLOR;
                    } else if (potion == ModPotionTypes.PYROMANCY.get() || potion == ModPotionTypes.PYROMANCY_LONG.get() ||
                                potion == ModPotionTypes.PYROMANCY_STRONG.get()) {
                        return PYROMANCY_COLOR;
                    } else if (potion == ModPotionTypes.MAGMA_WALKER.get() || potion == ModPotionTypes.MAGMA_WALKER_LONG.get()) {
                        return MAGMA_WALKER_COLOR;
                    } else if (potion == ModPotionTypes.MAGMA_WALKER_STRONG.get()) {
                        return MAGMA_WALKER_COLOR;
                    }
                    
                    // No tint if no specific color defined
                    return 0xFFFFFF;
                } else {
                    // Layer 1 (bottle) - no tint
                    return 0xFFFFFF;
                }
            }
            
            // For non-lava potions, use vanilla behavior
            if (tintIndex == 0) {
                return PotionUtils.getColor(stack); // Tint liquid layer with potion color
            } else {
                return 0xFFFFFF; // No tint for bottle layer
            }
        }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
    }
}
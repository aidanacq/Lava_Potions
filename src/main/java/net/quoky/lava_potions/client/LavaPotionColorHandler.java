package net.quoky.lava_potions.client;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.effect.GlassSkinEffect;
import net.quoky.lava_potions.effect.NetheriteSkinEffect;
import net.quoky.lava_potions.effect.ObsidianSkinEffect;
import net.quoky.lava_potions.effect.ModEffects;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.ModPotionBrewingRecipes;

/**
 * Handles color tinting for lava potions
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LavaPotionColorHandler {
        
    // Effect colors from effect classes
    private static final int OBSIDIAN_SKIN_COLOR = 0x8e5de3; // From ObsidianSkinEffect
    private static final int NETHERITE_SKIN_COLOR = 0x9b8457; // From NetheriteSkinEffect
    private static final int GLASS_SKIN_COLOR = 0xc2f3ff; // From GlassSkinEffect
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register color handler for vanilla potion items
        event.register((stack, tintIndex) -> {
            // Handle lava potions
            if (ModPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
                Potion potion = PotionUtils.getPotion(stack);
                
                // Only apply tint to the liquid layer (layer0)
                if (tintIndex == 0) {
                    // All lava potions use original_lava_contents.png with appropriate tinting
                    if (potion == ModPotionTypes.LAVA_BOTTLE.get() || potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                        return 0xFFFFFF; // No tint for base potions
                    } else if (potion == ModPotionTypes.OBSIDIAN_SKIN.get() || potion == ModPotionTypes.OBSIDIAN_SKIN_LONG.get()) {
                        return OBSIDIAN_SKIN_COLOR;
                    } else if (potion == ModPotionTypes.NETHERITE_SKIN.get() || potion == ModPotionTypes.NETHERITE_SKIN_LONG.get()) {
                        return NETHERITE_SKIN_COLOR;
                    } else if (potion == ModPotionTypes.GLASS_SKIN.get() || potion == ModPotionTypes.GLASS_SKIN_LONG.get() ||
                                potion == ModPotionTypes.GLASS_SKIN_STRONG.get()) {
                        return GLASS_SKIN_COLOR;
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
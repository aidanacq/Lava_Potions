package net.quoky.lava_potions.client;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

/**
 * Handles color tinting for lava potions to prevent unwanted tinting
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LavaPotionColorHandler {
    
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        // Register color handler for vanilla potion items
        event.register((stack, tintIndex) -> {
            // Only handle lava potions - let vanilla handle all others
            if (VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
                // Return white (no tint) for all layers to prevent color tinting
                return 0xFFFFFF;
            }
            
            if (tintIndex == 1) {
                return 0xFFFFFF; // No tint for bottle layer
            } else {
                return PotionUtils.getColor(stack); // Tint liquid layer with potion color
            }
        }, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
    }
}
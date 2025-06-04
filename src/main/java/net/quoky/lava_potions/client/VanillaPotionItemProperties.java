package net.quoky.lava_potions.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.potion.VanillaPotionBrewingRecipes;

/**
 * Handles item properties for vanilla potion items to display lava potion textures
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VanillaPotionItemProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger("VanillaPotionItemProperties");
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Registering vanilla potion item properties for lava potions");
            
            // Register properties for vanilla potion items
            registerItemProperties();
        });
    }
    
    /**
     * Register item properties for vanilla potion items
     */
    public static void registerItemProperties() {
        // Register property for regular potions
        ItemProperties.register(Items.POTION, 
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "lava_type"), 
            (stack, level, entity, seed) -> {
                if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
                    return 0.0F;
                }
                
                Potion potion = PotionUtils.getPotion(stack);
                if (potion == ModPotionTypes.LAVA_BOTTLE.get()) {
                    return 1.0F; // Lava bottle
                } else if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                    return 2.0F; // Awkward lava
                }
                
                return 0.0F;
            });
        
        // Register property for splash potions
        ItemProperties.register(Items.SPLASH_POTION, 
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "lava_type"), 
            (stack, level, entity, seed) -> {
                if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
                    return 0.0F;
                }
                
                Potion potion = PotionUtils.getPotion(stack);
                if (potion == ModPotionTypes.LAVA_BOTTLE.get()) {
                    return 1.0F; // Splash lava bottle
                } else if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                    return 2.0F; // Splash awkward lava
                }
                
                return 0.0F;
            });
        
        // Register property for lingering potions
        ItemProperties.register(Items.LINGERING_POTION, 
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "lava_type"), 
            (stack, level, entity, seed) -> {
                if (!VanillaPotionBrewingRecipes.isVanillaPotionWithLavaType(stack)) {
                    return 0.0F;
                }
                
                Potion potion = PotionUtils.getPotion(stack);
                if (potion == ModPotionTypes.LAVA_BOTTLE.get()) {
                    return 1.0F; // Lingering lava bottle
                } else if (potion == ModPotionTypes.AWKWARD_LAVA.get()) {
                    return 2.0F; // Lingering awkward lava
                }
                
                return 0.0F;
            });
    }
} 
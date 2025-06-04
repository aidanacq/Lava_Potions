package net.quoky.lava_potions.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.item.LavaPotionItem;
import net.quoky.lava_potions.item.ModItems;
import net.quoky.lava_potions.potion.ModPotionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LavaPotionItemProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger("LavaPotionItemProperties");
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerItemProperties(FMLClientSetupEvent event) {
        LOGGER.info("Registering lava potion item properties");
        event.enqueueWork(() -> {
            // Register potion type property
            registerLavaPotionProperty(ModItems.LAVA_POTION.get());
            
            // Register potion type modifier property (splash, lingering)
            registerPotionModifierProperty(ModItems.LAVA_POTION.get());
            
            LOGGER.info("Registered all lava potion item properties");
        });
    }
    
    private static void registerLavaPotionProperty(Item item) {
        LOGGER.info("Registering potion type property");
        ItemProperties.register(item, new ResourceLocation(Lava_Potions.MOD_ID, "potion"), 
            (stack, level, entity, seed) -> {
                // Get the potion type from the item's NBT
                String potionId = stack.getOrCreateTag().getString("Potion");
                LOGGER.debug("Checking potion type property for: {}", potionId);
                
                // Return different values based on the potion type
                if (potionId.contains("lava_bottle")) {
                    return 0.0F; // Regular lava bottle
                } else if (potionId.contains("awkward_lava")) {
                    return 1.0F; // Awkward lava potion
                } else if (potionId.contains("strength_lava")) {
                    return 2.0F; // Strength lava potion
                }
                
                // Default to regular lava bottle
                return 0.0F;
            });
    }
    
    private static void registerPotionModifierProperty(Item item) {
        LOGGER.info("Registering potion modifier property");
        ItemProperties.register(item, new ResourceLocation(Lava_Potions.MOD_ID, "potion_modifier"), 
            (stack, level, entity, seed) -> {
                // Get the potion type from the item's NBT
                String potionId = stack.getOrCreateTag().getString("Potion");
                LOGGER.debug("Checking potion modifier property for: {}", potionId);
                
                // Return different values based on potion modifier
                if (potionId.contains("splash")) {
                    LOGGER.debug("Detected splash potion: {}", potionId);
                    return 1.0F; // Splash variant
                } else if (potionId.contains("lingering")) {
                    LOGGER.debug("Detected lingering potion: {}", potionId);
                    return 2.0F; // Lingering variant
                }
                
                // Default to regular (drinkable) potion
                return 0.0F;
            });
    }
} 
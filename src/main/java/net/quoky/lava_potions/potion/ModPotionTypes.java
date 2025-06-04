package net.quoky.lava_potions.potion;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.quoky.lava_potions.Lava_Potions;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModPotionTypes {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModPotionTypes");
    
    // DeferredRegister for potions
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Lava_Potions.MOD_ID);
    
    // Only register the 2 base potion types - splash/lingering variants will be handled by vanilla system
    public static final RegistryObject<Potion> LAVA_BOTTLE = POTIONS.register("lava_bottle", 
        () -> new Potion());
        
    public static final RegistryObject<Potion> AWKWARD_LAVA = POTIONS.register("awkward_lava", 
        () -> new Potion());
    
    // Collection of base potion types for creative tab (only the 2 base types)
    public static final List<Potion> POTION_TYPES = new ArrayList<>();
    
    /**
     * Gets a potion type from its ID string
     */
    public static Potion getPotionTypeFromId(String id) {
        LOGGER.debug("Getting potion type from ID: {}", id);
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        if (resourceLocation != null) {
            Potion potion = ForgeRegistries.POTIONS.getValue(resourceLocation);
            if (potion != null) {
                return potion;
            } else {
                LOGGER.warn("Potion not found for ID: {}", id);
            }
        } else {
            LOGGER.warn("Invalid potion ID format: {}", id);
        }
        return LAVA_BOTTLE.get(); // Default
    }
    
    /**
     * Gets the registry ID for a potion type
     */
    public static String getPotionTypeId(Potion potion) {
        ResourceLocation resourceLocation = ForgeRegistries.POTIONS.getKey(potion);
        if (resourceLocation != null) {
            return resourceLocation.toString();
        }
        return Lava_Potions.MOD_ID + ":lava_bottle"; // Default
    }
    
    /**
     * Check if a potion is a splash variant (based on the item type, not potion type)
     */
    public static boolean isSplashPotion(Potion potion) {
        // This method is now used differently - it checks if we're dealing with splash items
        // The actual splash/lingering detection will be done at the item level
        return false; // Base potion types are never splash
    }
    
    /**
     * Check if a potion is a lingering variant (based on the item type, not potion type)
     */
    public static boolean isLingeringPotion(Potion potion) {
        // This method is now used differently - it checks if we're dealing with lingering items
        // The actual splash/lingering detection will be done at the item level
        return false; // Base potion types are never lingering
    }
    
    /**
     * Check if this is a base lava bottle
     */
    public static boolean isBaseLavaBottle(Potion potion) {
        return potion == LAVA_BOTTLE.get();
    }
    
    /**
     * Check if this is an awkward lava potion
     */
    public static boolean isAwkwardLava(Potion potion) {
        return potion == AWKWARD_LAVA.get();
    }
    
    /**
     * Check if this is any lava potion type
     */
    public static boolean isLavaPotion(Potion potion) {
        return isBaseLavaBottle(potion) || isAwkwardLava(potion);
    }
    
    /**
     * Initializes the potion types list for creative tab
     */
    private static void initPotionTypes() {
        LOGGER.info("Initializing potion types - registering only 2 base types");
        // Only add the 2 base potions - splash/lingering will be handled by vanilla system
        POTION_TYPES.add(LAVA_BOTTLE.get());
        POTION_TYPES.add(AWKWARD_LAVA.get());
        
        LOGGER.info("Added {} base potion types to creative tab", POTION_TYPES.size());
        LOGGER.info("Splash and lingering variants will be handled by vanilla brewing system");
    }
    
    /**
     * Registers all potion types
     */
    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
    
    /**
     * Event handler to initialize potion types after registration is complete
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModPotionTypes::initPotionTypes);
    }
} 
package net.quoky.lava_potions.potion;

import java.util.ArrayList;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModPotionTypes {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModPotionTypes");
    
    // DeferredRegister for potions
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Lava_Potions.MOD_ID);
    
    // Register lava bottle as a potion type
    public static final RegistryObject<Potion> LAVA_BOTTLE = POTIONS.register("lava_bottle", 
        () -> new Potion());
        
    // Register awkward lava potion
    public static final RegistryObject<Potion> AWKWARD_LAVA = POTIONS.register("awkward_lava", 
        () -> new Potion());
    
    // Register splash variants
    public static final RegistryObject<Potion> SPLASH_LAVA_BOTTLE = POTIONS.register("splash_lava_bottle", 
        () -> new Potion());
        
    public static final RegistryObject<Potion> SPLASH_AWKWARD_LAVA = POTIONS.register("splash_awkward_lava", 
        () -> new Potion());
    
    // Register lingering variants
    public static final RegistryObject<Potion> LINGERING_LAVA_BOTTLE = POTIONS.register("lingering_lava_bottle", 
        () -> new Potion());
        
    public static final RegistryObject<Potion> LINGERING_AWKWARD_LAVA = POTIONS.register("lingering_awkward_lava", 
        () -> new Potion());
    
    // We'll add more potion types later once we have the effects registered
    
    // Collection of all potion types for creative tab
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
     * Check if a potion is a splash variant
     */
    public static boolean isSplashPotion(Potion potion) {
        String id = getPotionTypeId(potion);
        return id.contains("splash");
    }
    
    /**
     * Check if a potion is a lingering variant
     */
    public static boolean isLingeringPotion(Potion potion) {
        String id = getPotionTypeId(potion);
        return id.contains("lingering");
    }
    
    /**
     * Get the corresponding splash variant for a potion
     */
    public static Potion getSplashVariant(Potion potion) {
        if (potion == LAVA_BOTTLE.get()) {
            return SPLASH_LAVA_BOTTLE.get();
        } else if (potion == AWKWARD_LAVA.get()) {
            return SPLASH_AWKWARD_LAVA.get();
        }
        // Add more mappings as needed
        return potion; // Return the same potion if no splash variant exists
    }
    
    /**
     * Get the corresponding lingering variant for a splash potion
     */
    public static Potion getLingeringVariant(Potion potion) {
        if (potion == SPLASH_LAVA_BOTTLE.get()) {
            return LINGERING_LAVA_BOTTLE.get();
        } else if (potion == SPLASH_AWKWARD_LAVA.get()) {
            return LINGERING_AWKWARD_LAVA.get();
        }
        // Add more mappings as needed
        return potion; // Return the same potion if no lingering variant exists
    }
    
    /**
     * Initializes the potion types list for creative tab
     */
    private static void initPotionTypes() {
        LOGGER.info("Initializing potion types");
        POTION_TYPES.add(LAVA_BOTTLE.get());
        POTION_TYPES.add(AWKWARD_LAVA.get());
        POTION_TYPES.add(SPLASH_LAVA_BOTTLE.get());
        POTION_TYPES.add(SPLASH_AWKWARD_LAVA.get());
        POTION_TYPES.add(LINGERING_LAVA_BOTTLE.get());
        POTION_TYPES.add(LINGERING_AWKWARD_LAVA.get());
        // We'll add more when we register them
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
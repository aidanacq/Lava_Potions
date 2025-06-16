package net.quoky.lava_potions.fluid;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Fluid utility class - previously registered custom fluids 
 * but now leverages Create's potion fluid system with texture overrides
 */
public class ModFluids {
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    
    /**
     * Register fluid event handling only if Create mod is loaded
     */
    public static void register(IEventBus eventBus) {
        if (CREATE_LOADED) {
            Lava_Potions.LOGGER.info("Create mod detected - using Create's potion fluid system with texture overrides");
        } else {
            Lava_Potions.LOGGER.info("Create mod not detected");
        }
    }
} 
package net.quoky.lava_potions.util;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Wrapper class to safely handle Create fluid registration
 * This class avoids importing Create classes directly to prevent
 * ClassNotFoundException
 */
public class CreateFluidCompat {

    /**
     * Safely register Create fluids if Create mod is present
     */
    public static void registerCreateFluids(IEventBus eventBus) {
        if (ModList.get().isLoaded("create")) {
            try {
                // Use reflection to avoid direct class loading
                Class<?> modFluidsClass = Class.forName("net.quoky.lava_potions.fluid.ModFluids");
                modFluidsClass.getMethod("register", IEventBus.class).invoke(null, eventBus);
                Lava_Potions.LOGGER.info("Create mod detected - registered custom lava potion fluids");
            } catch (Exception e) {
                Lava_Potions.LOGGER.warn("Failed to register Create fluids: {}", e.getMessage());
            }
        } else {
            Lava_Potions.LOGGER.info("Create mod not detected - skipping fluid registration");
        }
    }
}
package net.quoky.lava_potions.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.quoky.lava_potions.Lava_Potions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes sure everything is registered properly between hot reloads
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RunDataCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger("RunDataCleaner");
    
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        LOGGER.info("Making sure LavaBottleHandler is registered...");
        
        // Ensure the LavaBottleHandler is registered with the Forge event bus
        MinecraftForge.EVENT_BUS.unregister(LavaBottleHandler.class); // Prevent double registration
        MinecraftForge.EVENT_BUS.register(LavaBottleHandler.class);
        
        LOGGER.info("LavaBottleHandler registration ensured!");
    }
} 
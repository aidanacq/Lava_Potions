package net.quoky.lava_potions;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.quoky.lava_potions.effect.ModEffects;
import net.quoky.lava_potions.entity.ModEntityTypes;
import net.quoky.lava_potions.item.ModCreativeTabs;
import net.quoky.lava_potions.item.ModItems;
import net.quoky.lava_potions.potion.BrewingRecipes;
import net.quoky.lava_potions.potion.ModPotionTypes;
import net.quoky.lava_potions.util.LavaBottleHandler;

/**
 * Main mod class for Lava Potions
 */
@Mod(Lava_Potions.MOD_ID)
public class Lava_Potions {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "lava_potions";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructor for the mod
     */
    public Lava_Potions(FMLJavaModLoadingContext context) {
        // Get the mod event bus
        IEventBus modEventBus = context.getModEventBus();

        // Register items
        ModItems.register(modEventBus);
        
        // Register effects
        ModEffects.register(modEventBus);
        
        // Register potion types
        ModPotionTypes.register(modEventBus);
        
        // Register entity types
        ModEntityTypes.register(modEventBus);
        
        // Register creative mode tabs
        ModCreativeTabs.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Register our lava bottle handler
        MinecraftForge.EVENT_BUS.register(LavaBottleHandler.class);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    /**
     * Common setup event handler - registers brewing recipes
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(BrewingRecipes::registerBrewingRecipes);
        LOGGER.info("Lava Potions mod initialized!");
    }

    /**
     * Add items to vanilla creative tabs
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // We don't need to add the items to vanilla tabs anymore since we have our own tab
        // The custom tab is populated in the ModCreativeTabs class
    }
    
    /**
     * Server starting event handler
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Lava Potions server starting");
    }

    /**
     * Client-side setup
     */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Lava Potions client setup");
        }
    }
}

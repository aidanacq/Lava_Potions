// package net.quoky.lava_potions.util;

// import net.minecraft.resources.ResourceLocation;
// import net.minecraftforge.event.server.ServerStartedEvent;
// import net.minecraftforge.eventbus.api.SubscribeEvent;
// import net.minecraftforge.fml.ModList;
// import net.minecraftforge.fml.common.Mod;
// import net.quoky.lava_potions.Lava_Potions;

// /**
//  * Utility class to resolve recipe conflicts with other mods, specifically AlexsMobs
//  */
// @Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
// public class RecipeConflictResolver {

//     private static final boolean ALEXSMOBS_LOADED = ModList.get().isLoaded("alexsmobs");

//     /**
//      * Log conflict detection when server starts
//      */
//     @SubscribeEvent
//     public static void onServerStarted(ServerStartedEvent event) {
//         if (ALEXSMOBS_LOADED) {
//             Lava_Potions.LOGGER.info("AlexsMobs detected - using priority mixins to override lava bottle conflicts");
//             Lava_Potions.LOGGER.info("Recipe conflicts will be resolved through high-priority mixins and explicit recipes");
//         }
//     }
// }
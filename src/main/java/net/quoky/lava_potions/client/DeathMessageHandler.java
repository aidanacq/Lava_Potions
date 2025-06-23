package net.quoky.lava_potions.client;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;
import net.quoky.lava_potions.event.DecayableMagmaBlockEventHandler;

/**
 * Client-side event handler for custom death messages
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DeathMessageHandler {
    
    /**
     * Handle player respawn to clear any stored death messages
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player != null) {
            // Clear any stored death messages for this player
            DecayableMagmaBlockEventHandler.clearDeathMessages();
        }
    }
    
    /**
     * Handle player logout to clear stored death messages
     */
    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        // Clear all stored death messages when player logs out
        DecayableMagmaBlockEventHandler.clearDeathMessages();
    }
} 
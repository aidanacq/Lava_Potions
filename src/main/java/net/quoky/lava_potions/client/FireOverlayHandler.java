package net.quoky.lava_potions.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Handles client-side rendering events for the mod
 * Specifically manages the fire overlay when player has fire resistance
 */
@Mod.EventBusSubscriber(modid = Lava_Potions.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FireOverlayHandler {

    /**
     * Event handler to disable fire overlay when player has fire resistance
     * This prevents the annoying fire overlay from blocking vision when the player
     * is protected by fire resistance potion effect
     */
    @SubscribeEvent
    public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        // Check if the overlay type is fire
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) {
            // Get the player instance from Minecraft client
            Player player = Minecraft.getInstance().player;

            // Check if player exists and has fire resistance effect
            if (player != null && player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                // Cancel the fire overlay rendering
                event.setCanceled(true);
            }
        }
    }
}
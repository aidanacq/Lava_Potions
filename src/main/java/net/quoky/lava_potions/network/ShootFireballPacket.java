package net.quoky.lava_potions.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.quoky.lava_potions.effect.ModEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Packet for shooting fireballs with Fire Avatar effect
 */
public class ShootFireballPacket {
    // Track last fireball shot time for each player (server-side)
    private static final Map<UUID, Long> lastFireballTime = new HashMap<>();
    private static final long FIREBALL_COOLDOWN = 1500; // 1.5 seconds in milliseconds

    public ShootFireballPacket() {
        // Empty constructor for packet creation
    }

    /**
     * Encode packet data to buffer
     */
    public void encode(FriendlyByteBuf buffer) {
        // No data needs to be sent - just the action
    }

    /**
     * Decode packet data from buffer
     */
    public static ShootFireballPacket decode(FriendlyByteBuf buffer) {
        return new ShootFireballPacket();
    }

    /**
     * Handle the packet on the server side
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        // Ensure we're on the server side
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null)
                    return;

                // Verify player has Pyromancy effect and empty main hand
                if (!player.hasEffect(ModEffects.PYROMANCY.get()) ||
                        !player.getMainHandItem().isEmpty()) {
                    return;
                }

                UUID playerId = player.getUUID();
                long currentTime = System.currentTimeMillis();

                // Check cooldown
                if (lastFireballTime.containsKey(playerId)) {
                    long timeSinceLastShot = currentTime - lastFireballTime.get(playerId);
                    if (timeSinceLastShot < FIREBALL_COOLDOWN) {
                        return; // Still on cooldown
                    }
                }

                // Update last shot time
                lastFireballTime.put(playerId, currentTime);

                int amplifier = player.getEffect(ModEffects.PYROMANCY.get()).getAmplifier();

                // Shoot the fireball
                shootFireball(player, amplifier);
            });
        }

        context.setPacketHandled(true);
    }

    /**
     * Create and shoot a fireball from the player
     */
    private void shootFireball(ServerPlayer player, int amplifier) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Calculate fireball spawn position (at player eye level)
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();

        // Spawn fireball slightly in front of player to avoid collision
        Vec3 spawnPos = eyePos.add(lookVec.scale(1.0));

        // Create large fireball (like ghast)
        // The last parameter is the explosion power: Tier I = 1, Tier II = 2
        int explosionPower = amplifier == 0 ? 1 : 2;
        LargeFireball fireball = new LargeFireball(serverLevel, player, lookVec.x, lookVec.y, lookVec.z,
                explosionPower);
        fireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        // Spawn the fireball
        serverLevel.addFreshEntity(fireball);
    }
}
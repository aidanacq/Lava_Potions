package net.quoky.lava_potions.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.quoky.lava_potions.Lava_Potions;

/**
 * Network packet registration and management
 */
public class ModPackets {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Lava_Potions.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    /**
     * Register all network packets
     */
    public static void register() {
        INSTANCE.messageBuilder(ShootFireballPacket.class, nextId())
                .encoder(ShootFireballPacket::encode)
                .decoder(ShootFireballPacket::decode)
                .consumerMainThread(ShootFireballPacket::handle)
                .add();
    }

    private static int nextId() {
        return packetId++;
    }
}
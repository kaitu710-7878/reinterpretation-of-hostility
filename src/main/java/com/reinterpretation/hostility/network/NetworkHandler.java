package com.reinterpretation.hostility.network;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ReinterpretationOfHostility.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId = 0;

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextId++,
                UnlockHostilityC2SPacket.class,
                UnlockHostilityC2SPacket::encode,
                UnlockHostilityC2SPacket::decode,
                UnlockHostilityC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++,
                SyncMobTraitsS2CPacket.class,
                SyncMobTraitsS2CPacket::encode,
                SyncMobTraitsS2CPacket::decode,
                SyncMobTraitsS2CPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                nextId++,
                SaveDebugEditorC2SPacket.class,
                SaveDebugEditorC2SPacket::encode,
                SaveDebugEditorC2SPacket::decode,
                SaveDebugEditorC2SPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToTracking(Entity entity, Object packet) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }
}

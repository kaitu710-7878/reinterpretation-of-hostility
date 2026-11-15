package com.reinterpretation.hostility.network;

import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UnlockHostilityC2SPacket {

    public UnlockHostilityC2SPacket() {
    }

    public static void encode(UnlockHostilityC2SPacket msg, FriendlyByteBuf buf) {
    }

    public static UnlockHostilityC2SPacket decode(FriendlyByteBuf buf) {
        return new UnlockHostilityC2SPacket();
    }

    public static void handle(UnlockHostilityC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                PlayerDifficultyCapability.get(sender).unlock(sender);
            }
        });
        ctx.setPacketHandled(true);
    }
}

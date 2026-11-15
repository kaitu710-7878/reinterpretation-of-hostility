package com.reinterpretation.hostility.network;

import com.reinterpretation.hostility.capability.MobTraitCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncMobTraitsS2CPacket {

    private final int entityId;
    private final int level;
    private final List<String> traitIds;

    public SyncMobTraitsS2CPacket(int entityId, int level, List<String> traitIds) {
        this.entityId = entityId;
        this.level = level;
        this.traitIds = traitIds;
    }

    public static void encode(SyncMobTraitsS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.level);
        buf.writeVarInt(msg.traitIds.size());
        for (String id : msg.traitIds) {
            buf.writeUtf(id);
        }
    }

    public static SyncMobTraitsS2CPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        int level = buf.readVarInt();
        int count = buf.readVarInt();
        List<String> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readUtf());
        }
        return new SyncMobTraitsS2CPacket(entityId, level, ids);
    }

    public static void handle(SyncMobTraitsS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg)));
        ctx.setPacketHandled(true);
    }

    private static void handleClient(SyncMobTraitsS2CPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(msg.entityId);
        if (entity instanceof LivingEntity living && MobTraitCapability.hasCap(living)) {
            MobTraitCapability cap = MobTraitCapability.get(living);
            cap.initialized = true;
            cap.level = msg.level;
            cap.traitIds.clear();
            cap.traitIds.addAll(msg.traitIds);
        }
    }
}

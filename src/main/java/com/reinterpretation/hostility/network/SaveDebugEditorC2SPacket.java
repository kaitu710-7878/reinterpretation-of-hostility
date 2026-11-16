package com.reinterpretation.hostility.network;

import com.reinterpretation.hostility.item.DebugMobEditorItem;
import com.reinterpretation.hostility.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SaveDebugEditorC2SPacket {

    private final InteractionHand hand;
    private final int level;
    private final List<String> traitIds;

    public SaveDebugEditorC2SPacket(InteractionHand hand, int level, List<String> traitIds) {
        this.hand = hand;
        this.level = level;
        this.traitIds = traitIds;
    }

    public static void encode(SaveDebugEditorC2SPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.hand);
        buf.writeVarInt(msg.level);
        buf.writeVarInt(msg.traitIds.size());
        for (String id : msg.traitIds) {
            buf.writeUtf(id);
        }
    }

    public static SaveDebugEditorC2SPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        int level = buf.readVarInt();
        int count = buf.readVarInt();
        List<String> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readUtf());
        }
        return new SaveDebugEditorC2SPacket(hand, level, ids);
    }

    public static void handle(SaveDebugEditorC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                ItemStack stack = sender.getItemInHand(msg.hand);
                if (stack.is(ModItems.DEBUG_MOB_EDITOR.get())) {
                    DebugMobEditorItem.saveConfig(stack, msg.level, msg.traitIds);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}

package com.reinterpretation.hostility.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SealedItem extends Item {

    public static final String TAG_SEALED_STACK = "sealed_stack";
    public static final String TAG_EXPIRE_TICK = "expire_tick";

    public SealedItem() {
        super(new Properties().stacksTo(1));
    }

    public static ItemStack createSealed(ItemStack original, long expireTick) {
        ItemStack sealed = new ItemStack(ModItems.SEALED_ITEM.get());
        CompoundTag tag = sealed.getOrCreateTag();
        tag.put(TAG_SEALED_STACK, original.save(new CompoundTag()));
        tag.putLong(TAG_EXPIRE_TICK, expireTick);
        return sealed;
    }

    public static boolean isExpired(ItemStack sealed, long gameTime) {
        if (!sealed.hasTag()) {
            return true;
        }
        CompoundTag tag = sealed.getTag();
        return tag == null || gameTime >= tag.getLong(TAG_EXPIRE_TICK);
    }

    public static ItemStack unwrap(ItemStack sealed) {
        if (!sealed.hasTag()) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = sealed.getTag();
        if (tag == null || !tag.contains(TAG_SEALED_STACK)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(TAG_SEALED_STACK));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (isExpired(stack, level.getGameTime())) {
            ItemStack restored = unwrap(stack);
            if (living instanceof Player player) {
                if (slotId >= 0 && slotId < player.getInventory().getContainerSize()
                        && player.getInventory().getItem(slotId) == stack) {
                    player.getInventory().setItem(slotId, restored);
                }
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.rohostility.sealed_item.desc").withStyle(ChatFormatting.RED));
    }
}

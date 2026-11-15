package com.reinterpretation.hostility.compat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class CuriosCompat {

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    public record CurioSlotEntry(String slotKey, ItemStack stack) {
    }

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return CURIOS_LOADED;
    }

    public static List<CurioSlotEntry> getEquippedCurios(LivingEntity entity) {
        List<CurioSlotEntry> result = new ArrayList<>();
        if (!CURIOS_LOADED) {
            return result;
        }
        Inner.collectEquipped(entity, result);
        return result;
    }

    public static Multimap<Attribute, AttributeModifier> getCurioAttributeModifiers(
            LivingEntity entity, String slotId, int index, ItemStack stack) {
        if (!CURIOS_LOADED || stack.isEmpty()) {
            return HashMultimap.create();
        }
        return Inner.getModifiers(entity, slotId, index, stack);
    }

    public static boolean replaceCurioSlot(LivingEntity entity, String slotId, int index,
                                          Function<ItemStack, ItemStack> replacer) {
        if (!CURIOS_LOADED) {
            return false;
        }
        return Inner.replaceSlot(entity, slotId, index, replacer);
    }

    public static void iterateCurioSlots(LivingEntity entity, Function<ItemStack, ItemStack> replacer) {
        if (!CURIOS_LOADED) {
            return;
        }
        Inner.iterateSlots(entity, replacer);
    }

    private static final class Inner {
        static void collectEquipped(LivingEntity entity, List<CurioSlotEntry> out) {
            CuriosApi.getCuriosInventory(entity).ifPresent((ICuriosItemHandler handler) -> {
                for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                    String slotId = entry.getKey();
                    IDynamicStackHandler stacks = entry.getValue().getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            out.add(new CurioSlotEntry("curios:" + slotId + ":" + i, stack));
                        }
                    }
                }
            });
        }

        static Multimap<Attribute, AttributeModifier> getModifiers(
                LivingEntity entity, String slotId, int index, ItemStack stack) {
            SlotContext ctx = new SlotContext(slotId, entity, index, false, true);
            UUID uuid = UUID.nameUUIDFromBytes(("rohostility_curio_" + slotId + "_" + index).getBytes());
            return CuriosApi.getAttributeModifiers(ctx, uuid, stack);
        }

        static boolean replaceSlot(LivingEntity entity, String slotId, int index,
                                   Function<ItemStack, ItemStack> replacer) {
            final boolean[] changed = {false};
            CuriosApi.getCuriosInventory(entity).ifPresent((ICuriosItemHandler handler) -> {
                handler.getStacksHandler(slotId).ifPresent(stacksHandler -> {
                    IDynamicStackHandler stacks = stacksHandler.getStacks();
                    if (index >= 0 && index < stacks.getSlots()) {
                        ItemStack current = stacks.getStackInSlot(index);
                        ItemStack next = replacer.apply(current);
                        if (next != null) {
                            stacks.setStackInSlot(index, next);
                            changed[0] = true;
                        }
                    }
                });
            });
            return changed[0];
        }

        static void iterateSlots(LivingEntity entity, Function<ItemStack, ItemStack> replacer) {
            CuriosApi.getCuriosInventory(entity).ifPresent((ICuriosItemHandler handler) -> {
                for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                    IDynamicStackHandler stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack current = stacks.getStackInSlot(i);
                        ItemStack next = replacer.apply(current);
                        if (next != null) {
                            stacks.setStackInSlot(i, next);
                        }
                    }
                }
            });
        }
    }
}

package com.reinterpretation.hostility.trait;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public final class UnrealityEnchantHelper {

    public static final String TAG_UNREALITY = "rohostility_unreality";
    private static final String KEY_ORIG = "orig";
    private static final String KEY_RED = "red";
    private static final String KEY_EXPIRE = "expire";
    private static final long DURATION_TICKS = 200L; // 10 seconds

    private record Candidate(ItemStack stack, ResourceLocation enchId, int currentOrig, int currentRed) {
    }

    private UnrealityEnchantHelper() {
    }

    public static boolean weakenRandomEnchantment(LivingEntity target, long gameTime) {
        List<Candidate> candidates = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = target.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            Map<Enchantment, Integer> activeEnchs = EnchantmentHelper.getEnchantments(stack);
            CompoundTag tag = stack.getTagElement(TAG_UNREALITY);
            Set<ResourceLocation> seen = new HashSet<>();

            for (Map.Entry<Enchantment, Integer> entry : activeEnchs.entrySet()) {
                ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey());
                if (id == null) {
                    continue;
                }
                seen.add(id);
                if (tag != null && tag.contains(id.toString())) {
                    CompoundTag info = tag.getCompound(id.toString());
                    candidates.add(new Candidate(stack, id, info.getInt(KEY_ORIG), info.getInt(KEY_RED)));
                } else if (entry.getValue() > 0) {
                    candidates.add(new Candidate(stack, id, entry.getValue(), 0));
                }
            }

            // Include enchantments temporarily reduced to level 0
            if (tag != null) {
                for (String key : tag.getAllKeys()) {
                    ResourceLocation id = ResourceLocation.tryParse(key);
                    if (id == null || seen.contains(id)) {
                        continue;
                    }
                    CompoundTag info = tag.getCompound(key);
                    candidates.add(new Candidate(stack, id, info.getInt(KEY_ORIG), info.getInt(KEY_RED)));
                }
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        Candidate chosen = candidates.get(target.getRandom().nextInt(candidates.size()));
        Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(chosen.enchId());
        if (ench == null) {
            return false;
        }

        CompoundTag unrealityTag = chosen.stack().getOrCreateTagElement(TAG_UNREALITY);
        String key = chosen.enchId().toString();
        int orig = chosen.currentOrig();
        int red = chosen.currentRed();
        if (orig - red > 0) {
            red += 1;
        }

        CompoundTag info = new CompoundTag();
        info.putInt(KEY_ORIG, orig);
        info.putInt(KEY_RED, red);
        info.putLong(KEY_EXPIRE, gameTime + DURATION_TICKS);
        unrealityTag.put(key, info);

        int newLevel = Math.max(0, orig - red);
        Map<Enchantment, Integer> map = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(chosen.stack()));
        if (newLevel <= 0) {
            map.remove(ench);
        } else {
            map.put(ench, newLevel);
        }
        EnchantmentHelper.setEnchantments(map, chosen.stack());
        return true;
    }

    public static void restoreExpiredOnEntity(LivingEntity entity, long gameTime) {
        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                restoreExpiredOnStack(player.getInventory().getItem(i), gameTime);
            }
        } else {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                restoreExpiredOnStack(entity.getItemBySlot(slot), gameTime);
            }
        }
    }

    public static void restoreExpiredOnStack(ItemStack stack, long gameTime) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_UNREALITY)) {
            return;
        }
        CompoundTag unrealityTag = root.getCompound(TAG_UNREALITY);
        List<String> expiredKeys = new ArrayList<>();
        for (String key : unrealityTag.getAllKeys()) {
            CompoundTag info = unrealityTag.getCompound(key);
            if (gameTime >= info.getLong(KEY_EXPIRE)) {
                expiredKeys.add(key);
            }
        }
        if (expiredKeys.isEmpty()) {
            return;
        }

        Map<Enchantment, Integer> map = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));
        for (String key : expiredKeys) {
            CompoundTag info = unrealityTag.getCompound(key);
            int orig = info.getInt(KEY_ORIG);
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(id);
                if (ench != null && orig > 0) {
                    map.put(ench, orig);
                }
            }
            unrealityTag.remove(key);
        }
        EnchantmentHelper.setEnchantments(map, stack);

        if (unrealityTag.isEmpty()) {
            root.remove(TAG_UNREALITY);
        }
    }
}

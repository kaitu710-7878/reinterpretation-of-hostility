package com.reinterpretation.hostility.capability;

import com.google.common.collect.Multimap;
import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.compat.CuriosCompat;
import com.reinterpretation.hostility.item.ModItems;
import com.reinterpretation.hostility.item.SealedItem;
import dev.xkmc.l2library.capability.entity.GeneralCapabilityHolder;
import dev.xkmc.l2library.capability.entity.GeneralCapabilityTemplate;
import dev.xkmc.l2serial.serialization.SerialClass;
import dev.xkmc.l2serial.serialization.SerialClass.SerialField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@SerialClass
public class EntityDebuffCapability extends GeneralCapabilityTemplate<LivingEntity, EntityDebuffCapability> {

    public static final Capability<EntityDebuffCapability> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final GeneralCapabilityHolder<LivingEntity, EntityDebuffCapability> HOLDER =
            new GeneralCapabilityHolder<>(
                    new ResourceLocation(ReinterpretationOfHostility.MODID, "entity_debuffs"),
                    CAPABILITY,
                    EntityDebuffCapability.class,
                    EntityDebuffCapability::new,
                    LivingEntity.class,
                    entity -> true
            );

    private static final UUID PLUNDER_MODIFIER_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20101");
    private static final UUID CORROSION_ARMOR_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20102");
    private static final UUID CORROSION_TOUGH_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20103");
    private static final UUID MENTAL_DISRUPTION_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20104");
    private static final UUID PARALYSIS_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20105");
    private static final ResourceLocation L2DT_MAGIC_DAMAGE_ID =
            new ResourceLocation("l2damagetracker", "magic_damage");
    private static final UUID APOCALYPSE_ADD_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20201");
    private static final UUID APOCALYPSE_MULT_BASE_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20202");
    private static final UUID APOCALYPSE_MULT_TOTAL_UUID =
            UUID.fromString("9e2f8a10-4b2c-4d3e-9a11-71f0e3a20203");

    @SerialField
    public ArrayList<Long> plunderExpireTicks = new ArrayList<>();

    @SerialField
    public ArrayList<Long> corrosionExpireTicks = new ArrayList<>();

    @SerialField
    public ArrayList<Long> mentalDisruptionExpireTicks = new ArrayList<>();

    @SerialField
    public ArrayList<Long> paralysisExpireTicks = new ArrayList<>();

    @SerialField
    public HashMap<String, Long> apocalypseSlots = new HashMap<>();

    private final Set<Attribute> appliedApocalypseAttributes = new HashSet<>();

    public static EntityDebuffCapability getOrNull(LivingEntity entity) {
        if (entity == null || !HOLDER.isProper(entity)) {
            return null;
        }
        return entity.getCapability(CAPABILITY).resolve().orElse(null);
    }

    public static EntityDebuffCapability get(LivingEntity entity) {
        return HOLDER.get(entity);
    }

    public static void register() {
        // Trigger static initialization of HOLDER
    }

    public void addPlunderStack(LivingEntity entity, long gameTime) {
        plunderExpireTicks.add(gameTime + 400L); // 20 seconds
        updatePlunderModifier(entity);
    }

    public void addCorrosionStack(LivingEntity entity, long gameTime) {
        corrosionExpireTicks.add(gameTime + 160L); // 8 seconds
        updateCorrosionModifiers(entity);
    }

    public void addMentalDisruptionStack(LivingEntity entity, long gameTime) {
        mentalDisruptionExpireTicks.add(gameTime + 160L); // 8 seconds
        updateMentalDisruptionModifier(entity);
    }

    public void addParalysisStack(LivingEntity entity, long gameTime) {
        paralysisExpireTicks.add(gameTime + 160L); // 8 seconds
        updateParalysisModifier(entity);
    }

    public void clearPlunder(LivingEntity entity) {
        if (!plunderExpireTicks.isEmpty()) {
            plunderExpireTicks.clear();
            updatePlunderModifier(entity);
        }
    }

    public static final String TAG_APOCALYPSE_EXPIRE = "rohostility_apocalypse_expire";

    public void applyApocalypse(LivingEntity entity, String slotKey, long gameTime) {
        long expireTick = gameTime + 100L; // 5 seconds
        apocalypseSlots.put(slotKey, expireTick);
        setStackApocalypseTag(entity, slotKey, expireTick);
        updateApocalypseModifiers(entity);
    }

    private void setStackApocalypseTag(LivingEntity entity, String slotKey, long expireTick) {
        if (slotKey.startsWith("vanilla:")) {
            EquipmentSlot slot = EquipmentSlot.byName(slotKey.substring("vanilla:".length()));
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                stack.getOrCreateTag().putLong(TAG_APOCALYPSE_EXPIRE, expireTick);
            }
        } else if (slotKey.startsWith("curios:") && CuriosCompat.isLoaded()) {
            for (CuriosCompat.CurioSlotEntry entry : CuriosCompat.getEquippedCurios(entity)) {
                if (entry.slotKey().equals(slotKey) && !entry.stack().isEmpty()) {
                    entry.stack().getOrCreateTag().putLong(TAG_APOCALYPSE_EXPIRE, expireTick);
                }
            }
        }
    }

    private void cleanupExpiredApocalypseTags(LivingEntity entity, long gameTime) {
        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                removeExpiredApocalypseTag(player.getInventory().getItem(i), gameTime);
            }
        } else {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                removeExpiredApocalypseTag(entity.getItemBySlot(slot), gameTime);
            }
        }
        if (CuriosCompat.isLoaded()) {
            for (CuriosCompat.CurioSlotEntry entry : CuriosCompat.getEquippedCurios(entity)) {
                removeExpiredApocalypseTag(entry.stack(), gameTime);
            }
        }
    }

    private static void removeExpiredApocalypseTag(ItemStack stack, long gameTime) {
        if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains(TAG_APOCALYPSE_EXPIRE)) {
            if (gameTime >= stack.getTag().getLong(TAG_APOCALYPSE_EXPIRE)) {
                stack.getTag().remove(TAG_APOCALYPSE_EXPIRE);
            }
        }
    }

    public void applySeal(LivingEntity entity, String slotKey, long gameTime) {
        long expireTick = gameTime + 5L; // 5 ticks
        if (slotKey.startsWith("vanilla:")) {
            EquipmentSlot slot = EquipmentSlot.byName(slotKey.substring("vanilla:".length()));
            ItemStack current = entity.getItemBySlot(slot);
            if (!current.isEmpty() && !current.is(ModItems.SEALED_ITEM.get())) {
                entity.setItemSlot(slot, SealedItem.createSealed(current, expireTick));
            }
        } else if (slotKey.startsWith("curios:") && CuriosCompat.isLoaded()) {
            String rest = slotKey.substring("curios:".length());
            int lastColon = rest.lastIndexOf(':');
            if (lastColon <= 0) {
                return;
            }
            String slotId = rest.substring(0, lastColon);
            int index;
            try {
                index = Integer.parseInt(rest.substring(lastColon + 1));
            } catch (NumberFormatException e) {
                return;
            }
            CuriosCompat.replaceCurioSlot(entity, slotId, index, current -> {
                if (!current.isEmpty() && !current.is(ModItems.SEALED_ITEM.get())) {
                    return SealedItem.createSealed(current, expireTick);
                }
                return null;
            });
        }
    }

    public void restoreSealedItems(LivingEntity entity, long gameTime, boolean force) {
        if (entity instanceof Player player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(ModItems.SEALED_ITEM.get()) && (force || SealedItem.isExpired(stack, gameTime))) {
                    player.getInventory().setItem(i, SealedItem.unwrap(stack));
                }
            }
            ItemStack carried = player.containerMenu.getCarried();
            if (carried.is(ModItems.SEALED_ITEM.get()) && (force || SealedItem.isExpired(carried, gameTime))) {
                player.containerMenu.setCarried(SealedItem.unwrap(carried));
            }
        } else {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = entity.getItemBySlot(slot);
                if (stack.is(ModItems.SEALED_ITEM.get()) && (force || SealedItem.isExpired(stack, gameTime))) {
                    entity.setItemSlot(slot, SealedItem.unwrap(stack));
                }
            }
        }

        if (CuriosCompat.isLoaded()) {
            CuriosCompat.iterateCurioSlots(entity, current -> {
                if (current.is(ModItems.SEALED_ITEM.get()) && (force || SealedItem.isExpired(current, gameTime))) {
                    return SealedItem.unwrap(current);
                }
                return null;
            });
        }
    }

    public void clearAll(LivingEntity entity) {
        plunderExpireTicks.clear();
        corrosionExpireTicks.clear();
        mentalDisruptionExpireTicks.clear();
        paralysisExpireTicks.clear();
        apocalypseSlots.clear();
        updatePlunderModifier(entity);
        updateCorrosionModifiers(entity);
        updateMentalDisruptionModifier(entity);
        updateParalysisModifier(entity);
        updateApocalypseModifiers(entity);
        restoreSealedItems(entity, 0L, true);
    }

    public void tick(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        long gameTime = entity.level().getGameTime();

        restoreSealedItems(entity, gameTime, false);
        cleanupExpiredApocalypseTags(entity, gameTime);

        if (!plunderExpireTicks.isEmpty() && plunderExpireTicks.removeIf(expire -> gameTime >= expire)) {
            updatePlunderModifier(entity);
        }
        if (!corrosionExpireTicks.isEmpty() && corrosionExpireTicks.removeIf(expire -> gameTime >= expire)) {
            updateCorrosionModifiers(entity);
        }
        if (!mentalDisruptionExpireTicks.isEmpty() && mentalDisruptionExpireTicks.removeIf(expire -> gameTime >= expire)) {
            updateMentalDisruptionModifier(entity);
        }
        if (!paralysisExpireTicks.isEmpty() && paralysisExpireTicks.removeIf(expire -> gameTime >= expire)) {
            updateParalysisModifier(entity);
        }

        if (!apocalypseSlots.isEmpty()) {
            apocalypseSlots.entrySet().removeIf(entry -> gameTime >= entry.getValue());
            updateApocalypseModifiers(entity);
        } else if (!appliedApocalypseAttributes.isEmpty()) {
            updateApocalypseModifiers(entity);
        }
    }

    private static void applyPercentageDebuffModifier(LivingEntity entity, Attribute attr, UUID uuid,
                                                      String name, int count, double perStack) {
        if (attr == null) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(attr);
        if (instance == null) {
            return;
        }
        instance.removeModifier(uuid);
        if (count > 0) {
            double factor = Math.max(-0.99, -perStack * count);
            instance.addTransientModifier(new AttributeModifier(
                    uuid, name, factor, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private void updatePlunderModifier(LivingEntity entity) {
        applyPercentageDebuffModifier(entity, Attributes.MAX_HEALTH, PLUNDER_MODIFIER_UUID,
                "rohostility_plunder", plunderExpireTicks.size(), 0.01);
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private void updateCorrosionModifiers(LivingEntity entity) {
        int count = corrosionExpireTicks.size();
        applyPercentageDebuffModifier(entity, Attributes.ARMOR, CORROSION_ARMOR_UUID,
                "rohostility_corrosion_armor", count, 0.01);
        applyPercentageDebuffModifier(entity, Attributes.ARMOR_TOUGHNESS, CORROSION_TOUGH_UUID,
                "rohostility_corrosion_tough", count, 0.005);
    }

    private void updateMentalDisruptionModifier(LivingEntity entity) {
        Attribute magicAttr = ForgeRegistries.ATTRIBUTES.getValue(L2DT_MAGIC_DAMAGE_ID);
        applyPercentageDebuffModifier(entity, magicAttr, MENTAL_DISRUPTION_UUID,
                "rohostility_mental_disruption", mentalDisruptionExpireTicks.size(), 0.015);
    }

    private void updateParalysisModifier(LivingEntity entity) {
        applyPercentageDebuffModifier(entity, Attributes.ATTACK_DAMAGE, PARALYSIS_UUID,
                "rohostility_paralysis", paralysisExpireTicks.size(), 0.01);
    }

    private void updateApocalypseModifiers(LivingEntity entity) {
        for (Attribute attr : appliedApocalypseAttributes) {
            AttributeInstance inst = entity.getAttribute(attr);
            if (inst != null) {
                inst.removeModifier(APOCALYPSE_ADD_UUID);
                inst.removeModifier(APOCALYPSE_MULT_BASE_UUID);
                inst.removeModifier(APOCALYPSE_MULT_TOTAL_UUID);
            }
        }
        appliedApocalypseAttributes.clear();

        if (apocalypseSlots.isEmpty()) {
            return;
        }

        Map<Attribute, double[]> penalties = new HashMap<>();

        for (String slotKey : apocalypseSlots.keySet()) {
            Multimap<Attribute, AttributeModifier> mods = getSlotModifiers(entity, slotKey);
            if (mods == null || mods.isEmpty()) {
                continue;
            }
            for (Map.Entry<Attribute, AttributeModifier> entry : mods.entries()) {
                AttributeModifier mod = entry.getValue();
                if (mod.getAmount() <= 0.0) {
                    continue;
                }
                double[] sums = penalties.computeIfAbsent(entry.getKey(), k -> new double[3]);
                switch (mod.getOperation()) {
                    case ADDITION -> sums[0] += mod.getAmount() * 0.1;
                    case MULTIPLY_BASE -> sums[1] += mod.getAmount() * 0.1;
                    case MULTIPLY_TOTAL -> sums[2] += mod.getAmount() * 0.1;
                }
            }
        }

        for (Map.Entry<Attribute, double[]> entry : penalties.entrySet()) {
            Attribute attr = entry.getKey();
            AttributeInstance inst = entity.getAttribute(attr);
            if (inst == null) {
                continue;
            }
            double[] sums = entry.getValue();
            boolean applied = false;
            if (sums[0] > 0.0) {
                inst.addTransientModifier(new AttributeModifier(
                        APOCALYPSE_ADD_UUID, "rohostility_apocalypse_add",
                        -sums[0], AttributeModifier.Operation.ADDITION));
                applied = true;
            }
            if (sums[1] > 0.0) {
                inst.addTransientModifier(new AttributeModifier(
                        APOCALYPSE_MULT_BASE_UUID, "rohostility_apocalypse_base",
                        -sums[1], AttributeModifier.Operation.MULTIPLY_BASE));
                applied = true;
            }
            if (sums[2] > 0.0) {
                double factor = Math.max(-0.99, -sums[2] / (1.0 + sums[2]));
                inst.addTransientModifier(new AttributeModifier(
                        APOCALYPSE_MULT_TOTAL_UUID, "rohostility_apocalypse_total",
                        factor, AttributeModifier.Operation.MULTIPLY_TOTAL));
                applied = true;
            }
            if (applied) {
                appliedApocalypseAttributes.add(attr);
            }
        }

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private Multimap<Attribute, AttributeModifier> getSlotModifiers(LivingEntity entity, String slotKey) {
        if (slotKey.startsWith("vanilla:")) {
            String name = slotKey.substring("vanilla:".length());
            EquipmentSlot slot = EquipmentSlot.byName(name);
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) {
                return null;
            }
            return stack.getAttributeModifiers(slot);
        } else if (slotKey.startsWith("curios:") && CuriosCompat.isLoaded()) {
            String rest = slotKey.substring("curios:".length());
            int lastColon = rest.lastIndexOf(':');
            if (lastColon <= 0) {
                return null;
            }
            String slotId = rest.substring(0, lastColon);
            int index;
            try {
                index = Integer.parseInt(rest.substring(lastColon + 1));
            } catch (NumberFormatException e) {
                return null;
            }
            for (CuriosCompat.CurioSlotEntry entry : CuriosCompat.getEquippedCurios(entity)) {
                if (entry.slotKey().equals(slotKey)) {
                    return CuriosCompat.getCurioAttributeModifiers(entity, slotId, index, entry.stack());
                }
            }
        }
        return null;
    }

    public static List<String> collectWeakenableSlots(LivingEntity entity) {
        List<String> slots = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && !stack.is(ModItems.SEALED_ITEM.get())
                    && hasPositiveModifiers(stack.getAttributeModifiers(slot))) {
                slots.add("vanilla:" + slot.getName());
            }
        }
        if (CuriosCompat.isLoaded()) {
            for (CuriosCompat.CurioSlotEntry entry : CuriosCompat.getEquippedCurios(entity)) {
                if (entry.stack().is(ModItems.SEALED_ITEM.get())) {
                    continue;
                }
                String rest = entry.slotKey().substring("curios:".length());
                int lastColon = rest.lastIndexOf(':');
                String slotId = rest.substring(0, lastColon);
                int index = Integer.parseInt(rest.substring(lastColon + 1));
                if (hasPositiveModifiers(CuriosCompat.getCurioAttributeModifiers(entity, slotId, index, entry.stack()))) {
                    slots.add(entry.slotKey());
                }
            }
        }
        return slots;
    }

    public static List<String> collectAllEquippedSlots(LivingEntity entity) {
        List<String> slots = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty() && !stack.is(ModItems.SEALED_ITEM.get())) {
                slots.add("vanilla:" + slot.getName());
            }
        }
        if (CuriosCompat.isLoaded()) {
            for (CuriosCompat.CurioSlotEntry entry : CuriosCompat.getEquippedCurios(entity)) {
                if (!entry.stack().is(ModItems.SEALED_ITEM.get())) {
                    slots.add(entry.slotKey());
                }
            }
        }
        return slots;
    }

    private static boolean hasPositiveModifiers(Multimap<Attribute, AttributeModifier> mods) {
        if (mods == null || mods.isEmpty()) {
            return false;
        }
        for (AttributeModifier mod : mods.values()) {
            if (mod.getAmount() > 0.0) {
                return true;
            }
        }
        return false;
    }
}

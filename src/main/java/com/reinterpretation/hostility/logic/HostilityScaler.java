package com.reinterpretation.hostility.logic;

import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.network.NetworkHandler;
import com.reinterpretation.hostility.network.SyncMobTraitsS2CPacket;
import com.reinterpretation.hostility.trait.TraitType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HostilityScaler {

    private static final UUID LEVEL_HEALTH_UUID =
            UUID.fromString("7a1c4b20-1122-4334-9556-88aabbcc0001");
    private static final UUID LEVEL_DAMAGE_UUID =
            UUID.fromString("7a1c4b20-1122-4334-9556-88aabbcc0002");

    private HostilityScaler() {
    }

    public static void tryInitializeMob(LivingEntity entity) {
        if (entity.level().isClientSide() || !MobTraitCapability.hasCap(entity)) {
            return;
        }
        MobTraitCapability cap = MobTraitCapability.get(entity);
        if (cap.initialized) {
            return;
        }
        cap.initialized = true;
        cap.lastDamageTick = entity.level().getGameTime();

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPlayer nearest = findNearestUnlockedPlayer(serverLevel, entity);
        if (nearest == null) {
            cap.level = 0;
            return;
        }

        int baseDanger = PlayerDifficultyCapability.get(nearest).getDangerLevel();
        int dimBonus = 0;
        if (serverLevel.dimension() == Level.NETHER) {
            dimBonus = ModConfig.COMMON.netherDimensionBonus.get();
        } else if (serverLevel.dimension() == Level.END) {
            dimBonus = ModConfig.COMMON.endDimensionBonus.get();
        }

        RandomSource random = entity.getRandom();
        int variance = ModConfig.COMMON.levelRandomVariance.get();
        int offset = variance > 0 ? random.nextInt(variance * 2 + 1) - variance : 0;

        int maxLevel = ModConfig.COMMON.maxLevel.get();
        int level = Mth.clamp(baseDanger + dimBonus + offset, 0, maxLevel);
        cap.level = level;

        if (level > 0) {
            applyLevelStats(entity, level);
        }

        if (level >= ModConfig.COMMON.traitMinLevel.get()) {
            rollTraits(cap, level, random);
        }

        syncToTracking(entity, cap);
    }

    private static ServerPlayer findNearestUnlockedPlayer(ServerLevel level, LivingEntity entity) {
        ServerPlayer best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (ServerPlayer player : level.players()) {
            if (!PlayerDifficultyCapability.get(player).isUnlocked()) {
                continue;
            }
            double distSq = player.distanceToSqr(entity);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = player;
            }
        }
        return best;
    }

    private static void applyLevelStats(LivingEntity entity, int level) {
        double modifier = ModConfig.COMMON.statScalingModifier.get();
        if (modifier <= 0.0) {
            return;
        }

        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(LEVEL_HEALTH_UUID);
            double healthBonus = level * ModConfig.COMMON.healthBonusPerLevel.get() * modifier;
            if (healthBonus > 0.0) {
                healthAttr.addPermanentModifier(new AttributeModifier(
                        LEVEL_HEALTH_UUID,
                        "rohostility_level_health",
                        healthBonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
                entity.setHealth(entity.getMaxHealth());
            }
        }

        AttributeInstance damageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(LEVEL_DAMAGE_UUID);
            double damageBonus = level * ModConfig.COMMON.damageBonusPerLevel.get() * modifier;
            if (damageBonus > 0.0) {
                damageAttr.addPermanentModifier(new AttributeModifier(
                        LEVEL_DAMAGE_UUID,
                        "rohostility_level_damage",
                        damageBonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    private static void rollTraits(MobTraitCapability cap, int level, RandomSource random) {
        int budget = level;
        int maxTraits = ModConfig.COMMON.maxTraitCount.get();

        while (cap.getTraitIds().size() < maxTraits && budget > 0) {
            List<TraitType> candidates = new ArrayList<>();
            int totalWeight = 0;

            for (TraitType type : TraitType.values()) {
                if (!type.isEnabled() || cap.hasTrait(type)) {
                    continue;
                }
                int cost = type.getCost();
                if (cost <= budget) {
                    candidates.add(type);
                    totalWeight += Math.max(1, type.getWeight());
                }
            }

            if (candidates.isEmpty() || totalWeight <= 0) {
                break;
            }

            int pick = random.nextInt(totalWeight);
            TraitType chosen = null;
            int acc = 0;
            for (TraitType type : candidates) {
                acc += Math.max(1, type.getWeight());
                if (pick < acc) {
                    chosen = type;
                    break;
                }
            }

            if (chosen == null) {
                break;
            }

            cap.addTrait(chosen);
            budget -= chosen.getCost();
        }
    }

    public static void syncToTracking(LivingEntity entity, MobTraitCapability cap) {
        NetworkHandler.sendToTracking(entity, new SyncMobTraitsS2CPacket(
                entity.getId(), cap.level, new ArrayList<>(cap.getTraitIds())
        ));
    }
}

package com.reinterpretation.hostility.event;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.EntityDebuffCapability;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.logic.HostilityScaler;
import com.reinterpretation.hostility.network.NetworkHandler;
import com.reinterpretation.hostility.network.SyncMobTraitsS2CPacket;
import com.reinterpretation.hostility.trait.TraitType;
import com.reinterpretation.hostility.trait.UnrealityEnchantHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReinterpretationOfHostility.MODID)
public final class HostilityEventHandler {

    private HostilityEventHandler() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            HostilityScaler.tryInitializeMob(living);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getTarget() instanceof LivingEntity living
                && MobTraitCapability.hasCap(living)) {
            MobTraitCapability cap = MobTraitCapability.get(living);
            if (cap.initialized) {
                NetworkHandler.sendToPlayer(player, new SyncMobTraitsS2CPacket(
                        living.getId(), cap.level, new ArrayList<>(cap.getTraitIds())
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTo() == Level.NETHER) {
            PlayerDifficultyCapability.get(player).unlock(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension() == Level.NETHER) {
                PlayerDifficultyCapability.get(player).unlock(player);
            }
            PlayerDifficultyCapability.HOLDER.network.toClientSyncAll(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            EntityDebuffCapability.get(event.getEntity()).clearAll(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        long gameTime = entity.level().getGameTime();

        EntityDebuffCapability.get(entity).tick(entity);
        UnrealityEnchantHelper.restoreExpiredOnEntity(entity, gameTime);

        if (MobTraitCapability.hasCap(entity)) {
            MobTraitCapability cap = MobTraitCapability.get(entity);
            if (cap.hasTrait(TraitType.REGENERATION)
                    && entity.isAlive()
                    && entity.getHealth() < entity.getMaxHealth()
                    && gameTime - cap.lastDamageTick >= 160L) {
                entity.heal(entity.getMaxHealth() * 0.001f);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        DamageSource source = event.getSource();
        long gameTime = target.level().getGameTime();

        // Defensive traits on target mob
        if (MobTraitCapability.hasCap(target)) {
            MobTraitCapability targetCap = MobTraitCapability.get(target);
            targetCap.lastDamageTick = gameTime;

            if (targetCap.hasTrait(TraitType.FORTITUDE) && isMeleePhysical(source)) {
                event.setAmount(event.getAmount() * 0.30f);
            }

            if (targetCap.hasTrait(TraitType.PROTECTION) && isMagicDamage(source)) {
                event.setAmount(event.getAmount() * 0.30f);
            }

            if (targetCap.hasTrait(TraitType.ADAPTATION)) {
                String damageId = source.typeHolder().unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse(source.getMsgId());
                double mult = targetCap.applyAdaptationAndGetMultiplier(damageId);
                event.setAmount((float) (event.getAmount() * mult));
            }
        }

        // Offensive traits from attacker mob
        if (source.getEntity() instanceof LivingEntity attacker && MobTraitCapability.hasCap(attacker)) {
            MobTraitCapability attackerCap = MobTraitCapability.get(attacker);
            RandomSource rand = attacker.getRandom();

            if (attackerCap.hasTrait(TraitType.APOCALYPSE) && rand.nextFloat() < 0.30f) {
                List<String> slots = EntityDebuffCapability.collectWeakenableSlots(target);
                if (!slots.isEmpty()) {
                    String chosen = slots.get(rand.nextInt(slots.size()));
                    EntityDebuffCapability.get(target).applyApocalypse(target, chosen, gameTime);
                }
            }

            if (attackerCap.hasTrait(TraitType.DESTRUCTION)) {
                List<MobEffect> beneficial = new ArrayList<>();
                for (MobEffectInstance inst : target.getActiveEffects()) {
                    if (inst.getEffect().isBeneficial()) {
                        beneficial.add(inst.getEffect());
                    }
                }
                if (rand.nextFloat() < 0.40f) {
                    if (!beneficial.isEmpty()) {
                        MobEffect toRemove = beneficial.get(rand.nextInt(beneficial.size()));
                        target.removeEffect(toRemove);
                    }
                } else if (!beneficial.isEmpty()) {
                    event.setAmount(event.getAmount() * (1.0f + 0.10f * beneficial.size()));
                }
            }

            if (attackerCap.hasTrait(TraitType.UNREALITY) && rand.nextFloat() < 0.40f) {
                UnrealityEnchantHelper.weakenRandomEnchantment(target, gameTime);
            }

            if (attackerCap.hasTrait(TraitType.PLUNDER) && rand.nextFloat() < 0.25f) {
                EntityDebuffCapability.get(target).addPlunderStack(target, gameTime);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeathRevive(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !MobTraitCapability.hasCap(entity)) {
            return;
        }
        MobTraitCapability cap = MobTraitCapability.get(entity);
        if (cap.hasTrait(TraitType.REVIVE) && !cap.reviveUsed) {
            cap.reviveUsed = true;
            event.setCanceled(true);
            entity.setHealth(entity.getMaxHealth());
            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.HOSTILE, 0.8f, 1.0f);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        24, 0.4, 0.5, 0.4, 0.2);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeathFinalize(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        EntityDebuffCapability.get(entity).clearAll(entity);

        if (MobTraitCapability.hasCap(entity)
                && event.getSource().getEntity() instanceof ServerPlayer killer) {
            PlayerDifficultyCapability playerCap = PlayerDifficultyCapability.get(killer);
            if (playerCap.isUnlocked()) {
                MobTraitCapability mobCap = MobTraitCapability.get(entity);
                double baseGain = ModConfig.COMMON.baseKillDifficultyGain.get();
                double levelBonus = mobCap.level * ModConfig.COMMON.mobLevelKillBonus.get();
                double gain = (baseGain + levelBonus) * ModConfig.COMMON.difficultyGainMultiplier.get();
                playerCap.addDangerPoints(killer, gain);
            }
        }
    }

    private static boolean isMeleePhysical(DamageSource source) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)) {
            return false;
        }
        return source.getEntity() instanceof LivingEntity
                && source.getDirectEntity() == source.getEntity();
    }

    private static boolean isMagicDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypeTags.WITCH_RESISTANT_TO);
    }
}

package com.reinterpretation.hostility.event;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.EntityDebuffCapability;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.damage.ModDamageTypes;
import com.reinterpretation.hostility.effect.ModEffects;
import com.reinterpretation.hostility.item.ModItems;
import com.reinterpretation.hostility.item.SealedItem;
import com.reinterpretation.hostility.logic.HostilityScaler;
import com.reinterpretation.hostility.network.NetworkHandler;
import com.reinterpretation.hostility.network.SyncMobTraitsS2CPacket;
import com.reinterpretation.hostility.trait.TraitType;
import com.reinterpretation.hostility.trait.UnrealityEnchantHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ReinterpretationOfHostility.MODID)
public final class HostilityEventHandler {

    private static final UUID BERSERKER_DMG_UUID = UUID.fromString("8b2c1000-0001-4000-8000-000000000001");
    private static final UUID BERSERKER_SPD_UUID = UUID.fromString("8b2c1000-0001-4000-8000-000000000002");
    private static final UUID BERSERKER_RNG_UUID = UUID.fromString("8b2c1000-0001-4000-8000-000000000003");

    private static final UUID INSPIRE_DMG_UUID = UUID.fromString("8b2c1000-0002-4000-8000-000000000001");
    private static final UUID INSPIRE_MOV_UUID = UUID.fromString("8b2c1000-0002-4000-8000-000000000002");
    private static final UUID INSPIRE_ARM_UUID = UUID.fromString("8b2c1000-0002-4000-8000-000000000003");

    private static boolean handlingCounterattack = false;
    private static boolean handlingPierceDamage = false;

    private record AttackerSnapshot(int level, Set<TraitType> traits) {
        boolean hasTrait(TraitType type) {
            return traits.contains(type);
        }
    }

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
                && event.getTarget() instanceof LivingEntity living) {
            MobTraitCapability cap = MobTraitCapability.getOrNull(living);
            if (cap != null && cap.initialized) {
                NetworkHandler.sendToPlayer(player, new SyncMobTraitsS2CPacket(
                        living.getId(), cap.level, new ArrayList<>(cap.getTraitIds())
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDifficultyCapability.get(player).onDimensionVisited(player, player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDifficultyCapability.get(player).onDimensionVisited(player, player.level());
            PlayerDifficultyCapability.HOLDER.network.toClientSyncAll(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            EntityDebuffCapability debuffCap = EntityDebuffCapability.getOrNull(event.getEntity());
            if (debuffCap != null) {
                debuffCap.clearAll(event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (stack.is(ModItems.SEALED_ITEM.get())) {
            event.getEntity().setItem(SealedItem.unwrap(stack));
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (stack.is(ModItems.SEALED_ITEM.get())) {
                drop.setItem(SealedItem.unwrap(stack));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUseTotem(LivingUseTotemEvent event) {
        if (event.getEntity().hasEffect(ModEffects.REVIVE_PREVENTION.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(ModEffects.CURSE.get())) {
            event.setAmount(event.getAmount() * 0.5f);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.isRemoved()) {
            return;
        }
        long gameTime = entity.level().getGameTime();

        EntityDebuffCapability debuffCap = EntityDebuffCapability.getOrNull(entity);
        if (debuffCap != null) {
            debuffCap.tick(entity);
        }
        UnrealityEnchantHelper.restoreExpiredOnEntity(entity, gameTime);

        MobTraitCapability cap = MobTraitCapability.getOrNull(entity);
        if (cap != null) {
            if (cap.hasTrait(TraitType.REGENERATION)
                    && entity.isAlive()
                    && entity.getHealth() < entity.getMaxHealth()
                    && gameTime - cap.lastDamageTick >= 160L) {
                entity.heal(entity.getMaxHealth() * 0.001f);
            }

            if (cap.hasTrait(TraitType.BERSERKER) && entity.isAlive()) {
                tickBerserker(entity, cap, gameTime);
            }

            if (cap.hasTrait(TraitType.INSPIRE) && entity.isAlive() && entity instanceof Mob mob) {
                tickInspire(mob, cap, gameTime);
            }
        }
    }

    private static void tickBerserker(LivingEntity entity, MobTraitCapability cap, long gameTime) {
        boolean active = entity.getHealth() < entity.getMaxHealth() * 0.5f;
        if (active) {
            setTransientModifier(entity, Attributes.ATTACK_DAMAGE, BERSERKER_DMG_UUID,
                    "rohostility_berserker_dmg", 0.10, AttributeModifier.Operation.MULTIPLY_TOTAL);
            setTransientModifier(entity, Attributes.ATTACK_SPEED, BERSERKER_SPD_UUID,
                    "rohostility_berserker_spd", 0.10, AttributeModifier.Operation.ADDITION);
            setTransientModifier(entity, ForgeMod.ENTITY_REACH.get(), BERSERKER_RNG_UUID,
                    "rohostility_berserker_rng", 1.0, AttributeModifier.Operation.ADDITION);
            if (gameTime % 20L == 0L
                    && gameTime - cap.lastDamageTick >= 60L
                    && entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(entity.getMaxHealth() * 0.001f);
            }
        } else {
            removeModifier(entity, Attributes.ATTACK_DAMAGE, BERSERKER_DMG_UUID);
            removeModifier(entity, Attributes.ATTACK_SPEED, BERSERKER_SPD_UUID);
            removeModifier(entity, ForgeMod.ENTITY_REACH.get(), BERSERKER_RNG_UUID);
        }
    }

    private static void tickInspire(Mob mob, MobTraitCapability cap, long gameTime) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Iterator<String> it = cap.inspiredVexUuids.iterator();
        int aliveCount = 0;
        while (it.hasNext()) {
            try {
                UUID uuid = UUID.fromString(it.next());
                Entity e = serverLevel.getEntity(uuid);
                if (e instanceof Vex vex && vex.isAlive()) {
                    aliveCount++;
                } else {
                    it.remove();
                }
            } catch (IllegalArgumentException ex) {
                it.remove();
            }
        }

        if (aliveCount > 0) {
            double bonus = 0.05 * aliveCount;
            setTransientModifier(mob, Attributes.ATTACK_DAMAGE, INSPIRE_DMG_UUID,
                    "rohostility_inspire_dmg", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
            setTransientModifier(mob, Attributes.MOVEMENT_SPEED, INSPIRE_MOV_UUID,
                    "rohostility_inspire_mov", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
            setTransientModifier(mob, Attributes.ARMOR, INSPIRE_ARM_UUID,
                    "rohostility_inspire_arm", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
        } else {
            removeModifier(mob, Attributes.ATTACK_DAMAGE, INSPIRE_DMG_UUID);
            removeModifier(mob, Attributes.MOVEMENT_SPEED, INSPIRE_MOV_UUID);
            removeModifier(mob, Attributes.ARMOR, INSPIRE_ARM_UUID);
        }

        if (gameTime - cap.lastInspireTick >= 400L && mob.getTarget() != null) {
            cap.lastInspireTick = gameTime;
            RandomSource rand = mob.getRandom();
            for (int i = 0; i < 3; i++) {
                Vex vex = EntityType.VEX.create(serverLevel);
                if (vex != null) {
                    double vx = mob.getX() + (rand.nextDouble() - 0.5) * 3.0;
                    double vy = mob.getY() + 1.0 + rand.nextDouble();
                    double vz = mob.getZ() + (rand.nextDouble() - 0.5) * 3.0;
                    vex.moveTo(vx, vy, vz, mob.getYRot(), 0.0f);
                    vex.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, null, null);
                    vex.setOwner(mob);
                    vex.setTarget(mob.getTarget());
                    MobTraitCapability vexCap = MobTraitCapability.getOrNull(vex);
                    if (vexCap != null) {
                        vexCap.inspireSummoned = true;
                        vexCap.initialized = true;
                        vexCap.level = 0;
                    }
                    serverLevel.addFreshEntity(vex);
                    cap.inspiredVexUuids.add(vex.getUUID().toString());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide() || handlingCounterattack || handlingPierceDamage) {
            return;
        }
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == target) {
            return;
        }
        MobTraitCapability cap = MobTraitCapability.getOrNull(target);
        if (cap == null || !cap.hasTrait(TraitType.COUNTERATTACK)) {
            return;
        }
        long gameTime = target.level().getGameTime();
        if (gameTime - cap.lastCounterTick < 120L) {
            return;
        }
        cap.lastCounterTick = gameTime;
        handlingCounterattack = true;
        try {
            attacker.hurt(target.damageSources().mobAttack(target), 3.0f);
            AttackerSnapshot snapshot = new AttackerSnapshot(cap.level, cap.getTraits());
            triggerOnHitTraits(target, snapshot, attacker, gameTime);
        } finally {
            handlingCounterattack = false;
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
        MobTraitCapability targetCap = MobTraitCapability.getOrNull(target);
        if (targetCap != null) {
            targetCap.lastDamageTick = gameTime;

            if (targetCap.hasTrait(TraitType.FORTITUDE) && isMeleePhysical(source)) {
                event.setAmount(event.getAmount() * 0.30f);
            }

            if (targetCap.hasTrait(TraitType.PROTECTION) && isMagicDamage(source)) {
                event.setAmount(event.getAmount() * 0.30f);
            }

            if (targetCap.hasTrait(TraitType.UNIVERSAL_RESISTANCE)
                    && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                event.setAmount(event.getAmount() * 0.80f);
            }

            if (targetCap.hasTrait(TraitType.ADAPTATION)) {
                String damageId = source.typeHolder().unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse(source.getMsgId());
                double mult = targetCap.applyAdaptationAndGetMultiplier(damageId);
                event.setAmount((float) (event.getAmount() * mult));
            }
        }

        if (handlingPierceDamage) {
            return;
        }

        // Offensive traits from attacker mob (supports discarded Creepers via PersistentData fallback)
        if (source.getEntity() instanceof LivingEntity attacker && MobTraitCapability.isHostileCandidate(attacker)) {
            AttackerSnapshot attackerSnap = getAttackerSnapshot(attacker);
            if (attackerSnap == null) {
                return;
            }

            // Apply level-based damage scaling for mobs without ATTACK_DAMAGE (e.g., Creeper explosions)
            if (attackerSnap.level() > 0 && attacker.getAttribute(Attributes.ATTACK_DAMAGE) == null) {
                double mod = ModConfig.COMMON.statScalingModifier.get();
                if (mod > 0.0) {
                    double bonus = attackerSnap.level() * ModConfig.COMMON.damageBonusPerLevel.get() * mod;
                    event.setAmount((float) (event.getAmount() * (1.0 + bonus)));
                }
            }

            RandomSource rand = attacker.getRandom();

            if (attackerSnap.hasTrait(TraitType.DESTRUCTION)) {
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

            if (!handlingCounterattack) {
                triggerOnHitTraits(attacker, attackerSnap, target, gameTime);
            }

            boolean hasArmorPierce = attackerSnap.hasTrait(TraitType.ARMOR_PIERCE);
            boolean hasProtPierce = attackerSnap.hasTrait(TraitType.PROTECTION_PIERCE);
            if (hasArmorPierce || hasProtPierce) {
                float baseAmount = event.getAmount();
                float pierceRatio = (hasArmorPierce ? 0.05f : 0.0f) + (hasProtPierce ? 0.05f : 0.0f);
                event.setAmount(baseAmount * (1.0f - pierceRatio));

                handlingPierceDamage = true;
                int prevInvul = target.invulnerableTime;
                try {
                    if (hasArmorPierce) {
                        target.invulnerableTime = 0;
                        target.hurt(ModDamageTypes.armorPierce(target.level(), attacker), baseAmount * 0.05f);
                    }
                    if (hasProtPierce) {
                        target.invulnerableTime = 0;
                        target.hurt(ModDamageTypes.protectionPierce(target.level(), attacker), baseAmount * 0.05f);
                    }
                } finally {
                    target.invulnerableTime = prevInvul;
                    handlingPierceDamage = false;
                }
            }
        }
    }

    private static AttackerSnapshot getAttackerSnapshot(LivingEntity attacker) {
        MobTraitCapability cap = MobTraitCapability.getOrNull(attacker);
        if (cap != null) {
            return new AttackerSnapshot(cap.level, cap.getTraits());
        }
        CompoundTag pdata = attacker.getPersistentData();
        if (pdata.contains("rohostility_lv")) {
            int lv = pdata.getInt("rohostility_lv");
            Set<TraitType> traits = EnumSet.noneOf(TraitType.class);
            if (pdata.contains("rohostility_traits", Tag.TAG_LIST)) {
                ListTag list = pdata.getList("rohostility_traits", Tag.TAG_STRING);
                for (int i = 0; i < list.size(); i++) {
                    TraitType t = TraitType.byId(list.getString(i));
                    if (t != null) {
                        traits.add(t);
                    }
                }
            }
            return new AttackerSnapshot(lv, traits);
        }
        return null;
    }

    private static void triggerOnHitTraits(LivingEntity attacker, AttackerSnapshot snap, LivingEntity target, long gameTime) {
        RandomSource rand = attacker.getRandom();
        EntityDebuffCapability targetDebuff = EntityDebuffCapability.getOrNull(target);

        if (targetDebuff != null && snap.hasTrait(TraitType.APOCALYPSE) && rand.nextFloat() < 0.30f) {
            List<String> slots = EntityDebuffCapability.collectWeakenableSlots(target);
            if (!slots.isEmpty()) {
                String chosen = slots.get(rand.nextInt(slots.size()));
                targetDebuff.applyApocalypse(target, chosen, gameTime);
            }
        }

        if (targetDebuff != null && snap.hasTrait(TraitType.SEAL) && rand.nextFloat() < 0.10f) {
            List<String> slots = EntityDebuffCapability.collectAllEquippedSlots(target);
            if (!slots.isEmpty()) {
                String chosen = slots.get(rand.nextInt(slots.size()));
                targetDebuff.applySeal(target, chosen, gameTime);
            }
        }

        if (snap.hasTrait(TraitType.UNREALITY) && rand.nextFloat() < 0.40f) {
            UnrealityEnchantHelper.weakenRandomEnchantment(target, gameTime);
        }

        if (targetDebuff != null && snap.hasTrait(TraitType.PLUNDER) && rand.nextFloat() < 0.25f) {
            targetDebuff.addPlunderStack(target, gameTime);
        }

        if (snap.hasTrait(TraitType.LIFESTEAL) && attacker.isAlive()) {
            attacker.heal(attacker.getMaxHealth() * 0.001f);
        }

        if (snap.hasTrait(TraitType.ANTI_REVIVE) && rand.nextFloat() < 0.50f) {
            target.addEffect(new MobEffectInstance(ModEffects.REVIVE_PREVENTION.get(), 160, 0));
        }

        if (snap.hasTrait(TraitType.POISON_STRIKE) && rand.nextFloat() < 0.50f) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0));
        }

        if (snap.hasTrait(TraitType.WEAKNESS_STRIKE) && rand.nextFloat() < 0.50f) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
        }

        if (snap.hasTrait(TraitType.RESENTMENT) && rand.nextFloat() < 0.40f) {
            target.addEffect(new MobEffectInstance(ModEffects.CURSE.get(), 160, 0));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeathRevive(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        MobTraitCapability cap = MobTraitCapability.getOrNull(entity);
        if (cap != null && cap.hasTrait(TraitType.REVIVE) && !cap.reviveUsed) {
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

        EntityDebuffCapability debuffCap = EntityDebuffCapability.getOrNull(entity);
        if (debuffCap != null) {
            debuffCap.clearAll(entity);
        }

        MobTraitCapability mobCap = MobTraitCapability.getOrNull(entity);
        if (mobCap != null && event.getSource().getEntity() instanceof ServerPlayer killer) {
            PlayerDifficultyCapability playerCap = PlayerDifficultyCapability.get(killer);
            if (playerCap.isUnlocked()) {
                playerCap.addKillCredit(killer, mobCap.level);
            }
        }
    }

    private static void setTransientModifier(LivingEntity entity, Attribute attr, UUID uuid,
                                             String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst == null) {
            return;
        }
        AttributeModifier existing = inst.getModifier(uuid);
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0 && existing.getOperation() == op) {
            return;
        }
        inst.removeModifier(uuid);
        inst.addTransientModifier(new AttributeModifier(uuid, name, amount, op));
    }

    private static void removeModifier(LivingEntity entity, Attribute attr, UUID uuid) {
        AttributeInstance inst = entity.getAttribute(attr);
        if (inst != null && inst.getModifier(uuid) != null) {
            inst.removeModifier(uuid);
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

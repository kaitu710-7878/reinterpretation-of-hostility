package com.reinterpretation.hostility.capability;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.trait.TraitType;
import dev.xkmc.l2library.capability.entity.GeneralCapabilityHolder;
import dev.xkmc.l2library.capability.entity.GeneralCapabilityTemplate;
import dev.xkmc.l2serial.serialization.SerialClass;
import dev.xkmc.l2serial.serialization.SerialClass.SerialField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SerialClass
public class MobTraitCapability extends GeneralCapabilityTemplate<LivingEntity, MobTraitCapability> {

    public static final Capability<MobTraitCapability> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final GeneralCapabilityHolder<LivingEntity, MobTraitCapability> HOLDER =
            new GeneralCapabilityHolder<>(
                    new ResourceLocation(ReinterpretationOfHostility.MODID, "mob_traits"),
                    CAPABILITY,
                    MobTraitCapability.class,
                    MobTraitCapability::new,
                    LivingEntity.class,
                    MobTraitCapability::isHostileCandidate
            );

    @SerialField
    public boolean initialized = false;

    @SerialField
    public int level = 0;

    @SerialField
    public ArrayList<String> traitIds = new ArrayList<>();

    @SerialField
    public boolean reviveUsed = false;

    @SerialField
    public long lastDamageTick = 0L;

    @SerialField
    public String adaptedDamageType = "";

    @SerialField
    public int adaptCount = 0;

    @SerialField
    public long lastCounterTick = -1000L;

    @SerialField
    public long lastInspireTick = 0L;

    @SerialField
    public ArrayList<String> inspiredVexUuids = new ArrayList<>();

    @SerialField
    public boolean inspireSummoned = false;

    public static boolean isHostileCandidate(LivingEntity entity) {
        return entity instanceof Mob && entity instanceof Enemy;
    }

    public static boolean hasCap(LivingEntity entity) {
        return isHostileCandidate(entity) && HOLDER.isProper(entity);
    }

    public static MobTraitCapability get(LivingEntity entity) {
        return HOLDER.get(entity);
    }

    public static void register() {
        // Trigger static initialization of HOLDER
    }

    public boolean hasTrait(TraitType type) {
        return traitIds.contains(type.getId());
    }

    public void addTrait(TraitType type) {
        if (!traitIds.contains(type.getId())) {
            traitIds.add(type.getId());
        }
    }

    public Set<TraitType> getTraits() {
        Set<TraitType> result = EnumSet.noneOf(TraitType.class);
        for (String id : traitIds) {
            TraitType type = TraitType.byId(id);
            if (type != null) {
                result.add(type);
            }
        }
        return result;
    }

    public List<String> getTraitIds() {
        return traitIds;
    }

    /**
     * Applies Adaptation logic: remembers only 1 damage type, reduces by 10% per hit
     * (1.0 -> 0.9 -> 0.81 -> 0.729...), floored at 0.5 (50%).
     */
    public double applyAdaptationAndGetMultiplier(String damageTypeId) {
        if (damageTypeId == null || damageTypeId.isEmpty()) {
            return 1.0;
        }
        if (adaptedDamageType.isEmpty()) {
            adaptedDamageType = damageTypeId;
            adaptCount = 1;
            return 1.0;
        }
        if (!adaptedDamageType.equals(damageTypeId)) {
            return 1.0;
        }
        double mult = Math.max(0.5, Math.pow(0.9, adaptCount));
        adaptCount++;
        return mult;
    }
}

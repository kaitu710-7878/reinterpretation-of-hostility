package com.reinterpretation.hostility.damage;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class ModDamageTypes {

    public static final ResourceKey<DamageType> ARMOR_PIERCE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(ReinterpretationOfHostility.MODID, "armor_pierce"));

    public static final ResourceKey<DamageType> PROTECTION_PIERCE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(ReinterpretationOfHostility.MODID, "protection_pierce"));

    private ModDamageTypes() {
    }

    public static DamageSource armorPierce(Level level, Entity attacker) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ARMOR_PIERCE),
                attacker
        );
    }

    public static DamageSource protectionPierce(Level level, Entity attacker) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PROTECTION_PIERCE),
                attacker
        );
    }
}

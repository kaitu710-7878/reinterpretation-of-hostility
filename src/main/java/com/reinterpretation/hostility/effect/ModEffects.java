package com.reinterpretation.hostility.effect;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ReinterpretationOfHostility.MODID);

    public static final RegistryObject<MobEffect> REVIVE_PREVENTION =
            MOB_EFFECTS.register("revive_prevention", () -> new SimpleHostilityEffect(MobEffectCategory.NEUTRAL, 0x8855AA));

    public static final RegistryObject<MobEffect> CURSE =
            MOB_EFFECTS.register("curse", () -> new SimpleHostilityEffect(MobEffectCategory.HARMFUL, 0x4A0055));

    private ModEffects() {
    }

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }

    private static class SimpleHostilityEffect extends MobEffect {
        protected SimpleHostilityEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }
}

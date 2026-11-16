package com.reinterpretation.hostility.config;

import com.reinterpretation.hostility.trait.TraitType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.EnumMap;
import java.util.Map;

public class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue maxLevel;
        public final ForgeConfigSpec.DoubleValue maxDangerCap;
        public final ForgeConfigSpec.IntValue traitMinLevel;
        public final ForgeConfigSpec.IntValue maxTraitCount;
        public final ForgeConfigSpec.IntValue killsPerLevel;

        public final ForgeConfigSpec.DoubleValue healthBonusPerLevel;
        public final ForgeConfigSpec.DoubleValue damageBonusPerLevel;
        public final ForgeConfigSpec.DoubleValue statScalingModifier;

        public final ForgeConfigSpec.IntValue levelRandomVariance;

        public final ForgeConfigSpec.BooleanValue showMobOverheadInfo;

        public final Map<TraitType, TraitConfigEntry> traits = new EnumMap<>(TraitType.class);

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("General difficulty and progression settings")
                    .push("general");

            maxLevel = builder
                    .comment("Base maximum mob level cap (increased by End / modded dimension visits).")
                    .defineInRange("maxLevel", 600, 1, 10000);

            maxDangerCap = builder
                    .comment("Hard stopper cap for accumulated player danger points.")
                    .defineInRange("maxDangerCap", 2000.0, 1.0, 100000.0);

            traitMinLevel = builder
                    .comment("Minimum mob level required for traits to appear (1 = immediate when level >= trait cost).")
                    .defineInRange("traitMinLevel", 1, 1, 10000);

            maxTraitCount = builder
                    .comment("Maximum number of traits a single mob can receive.")
                    .defineInRange("maxTraitCount", 8, 1, 20);

            killsPerLevel = builder
                    .comment("Kills of same-level mob required per danger level (original L2H = 30, 60 = half pace).")
                    .defineInRange("killsPerLevel", 60, 1, 100000);

            levelRandomVariance = builder
                    .comment("Random level variance (+/-) applied when mobs spawn.")
                    .defineInRange("levelRandomVariance", 5, 0, 100);

            builder.pop();

            builder.comment("Stat scaling settings")
                    .push("scaling");

            healthBonusPerLevel = builder
                    .comment("Base max health bonus per mob level (0.03 = +3% per level, matching original L2H).")
                    .defineInRange("healthBonusPerLevel", 0.03, 0.0, 10.0);

            damageBonusPerLevel = builder
                    .comment("Base attack damage bonus per mob level (0.02 = +2% per level).")
                    .defineInRange("damageBonusPerLevel", 0.02, 0.0, 10.0);

            statScalingModifier = builder
                    .comment("Overall multiplier applied to level-based stat bonuses (< 1.0 weakens, > 1.0 strengthens).")
                    .defineInRange("statScalingModifier", 1.0, 0.0, 100.0);

            builder.pop();

            builder.comment("Client display settings")
                    .push("display");

            showMobOverheadInfo = builder
                    .comment("Show mob level and traits overhead when looking at a mob.")
                    .define("showMobOverheadInfo", true);

            builder.pop();

            builder.comment("Per-trait configuration (enabled, cost, weight)")
                    .push("traits");

            for (TraitType type : TraitType.values()) {
                builder.push(type.getId());
                ForgeConfigSpec.BooleanValue enabled = builder
                        .comment("Whether this trait can appear on mobs.")
                        .define("enabled", true);
                ForgeConfigSpec.IntValue cost = builder
                        .comment("Budget cost required for a mob to roll this trait.")
                        .defineInRange("cost", type.getDefaultCost(), 1, 1000);
                ForgeConfigSpec.IntValue weight = builder
                        .comment("Selection weight when rolling traits.")
                        .defineInRange("weight", type.getDefaultWeight(), 1, 1000);
                traits.put(type, new TraitConfigEntry(enabled, cost, weight));
                builder.pop();
            }

            builder.pop();
        }
    }

    public record TraitConfigEntry(
            ForgeConfigSpec.BooleanValue enabled,
            ForgeConfigSpec.IntValue cost,
            ForgeConfigSpec.IntValue weight
    ) {
    }
}

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
        public final ForgeConfigSpec.IntValue traitMinLevel;
        public final ForgeConfigSpec.IntValue maxTraitCount;
        public final ForgeConfigSpec.DoubleValue difficultyGainMultiplier;
        public final ForgeConfigSpec.DoubleValue baseKillDifficultyGain;
        public final ForgeConfigSpec.DoubleValue mobLevelKillBonus;

        public final ForgeConfigSpec.DoubleValue healthBonusPerLevel;
        public final ForgeConfigSpec.DoubleValue damageBonusPerLevel;
        public final ForgeConfigSpec.DoubleValue statScalingModifier;

        public final ForgeConfigSpec.IntValue netherDimensionBonus;
        public final ForgeConfigSpec.IntValue endDimensionBonus;
        public final ForgeConfigSpec.IntValue levelRandomVariance;

        public final ForgeConfigSpec.BooleanValue showMobOverheadInfo;

        public final Map<TraitType, TraitConfigEntry> traits = new EnumMap<>(TraitType.class);

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("General difficulty and progression settings")
                    .push("general");

            maxLevel = builder
                    .comment("Maximum mob level cap regardless of player danger level.")
                    .defineInRange("maxLevel", 600, 1, 10000);

            traitMinLevel = builder
                    .comment("Minimum mob level required for traits to appear.")
                    .defineInRange("traitMinLevel", 90, 1, 10000);

            maxTraitCount = builder
                    .comment("Maximum number of traits a single mob can receive.")
                    .defineInRange("maxTraitCount", 8, 1, 20);

            difficultyGainMultiplier = builder
                    .comment("Global multiplier for danger/difficulty gain from killing mobs (0.5 = half of original pace).")
                    .defineInRange("difficultyGainMultiplier", 0.5, 0.0, 100.0);

            baseKillDifficultyGain = builder
                    .comment("Base danger points gained per hostile mob kill before multiplier.")
                    .defineInRange("baseKillDifficultyGain", 1.0, 0.0, 1000.0);

            mobLevelKillBonus = builder
                    .comment("Additional danger points gained per level of the killed mob before multiplier.")
                    .defineInRange("mobLevelKillBonus", 0.05, 0.0, 100.0);

            levelRandomVariance = builder
                    .comment("Random level variance (+/-) applied when mobs spawn.")
                    .defineInRange("levelRandomVariance", 5, 0, 100);

            netherDimensionBonus = builder
                    .comment("Flat level bonus added in the Nether.")
                    .defineInRange("netherDimensionBonus", 20, 0, 600);

            endDimensionBonus = builder
                    .comment("Flat level bonus added in the End.")
                    .defineInRange("endDimensionBonus", 40, 0, 600);

            builder.pop();

            builder.comment("Stat scaling settings")
                    .push("scaling");

            healthBonusPerLevel = builder
                    .comment("Base max health bonus per mob level (0.05 = +5% per level).")
                    .defineInRange("healthBonusPerLevel", 0.05, 0.0, 10.0);

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

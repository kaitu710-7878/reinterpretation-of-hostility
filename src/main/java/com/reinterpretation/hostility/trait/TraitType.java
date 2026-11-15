package com.reinterpretation.hostility.trait;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum TraitType {
    REVIVE("revive", 150, 40, 0xFFD700),
    REGENERATION("regeneration", 50, 100, 0xFF5577),
    ADAPTATION("adaptation", 150, 80, 0x55CCFF),
    APOCALYPSE("apocalypse", 300, 60, 0xAA33FF),
    DESTRUCTION("destruction", 150, 70, 0xFF6600),
    UNREALITY("unreality", 200, 55, 0xCC77FF),
    FORTITUDE("fortitude", 150, 50, 0x8899AA),
    PROTECTION("protection", 150, 50, 0x5588FF),
    PLUNDER("plunder", 400, 35, 0xCC1133);

    private final String id;
    private final int defaultCost;
    private final int defaultWeight;
    private final int color;

    TraitType(String id, int defaultCost, int defaultWeight, int color) {
        this.id = id;
        this.defaultCost = defaultCost;
        this.defaultWeight = defaultWeight;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public int getDefaultCost() {
        return defaultCost;
    }

    public int getDefaultWeight() {
        return defaultWeight;
    }

    public int getColor() {
        return color;
    }

    public boolean isEnabled() {
        ModConfig.TraitConfigEntry entry = ModConfig.COMMON.traits.get(this);
        return entry == null || entry.enabled().get();
    }

    public int getCost() {
        ModConfig.TraitConfigEntry entry = ModConfig.COMMON.traits.get(this);
        return entry != null ? entry.cost().get() : defaultCost;
    }

    public int getWeight() {
        ModConfig.TraitConfigEntry entry = ModConfig.COMMON.traits.get(this);
        return entry != null ? entry.weight().get() : defaultWeight;
    }

    public String getTranslationKey() {
        return "trait." + ReinterpretationOfHostility.MODID + "." + id;
    }

    public String getDescTranslationKey() {
        return "trait." + ReinterpretationOfHostility.MODID + "." + id + ".desc";
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(getTranslationKey()).withStyle(style -> style.withColor(color));
    }

    public MutableComponent getDescription() {
        return Component.translatable(getDescTranslationKey());
    }

    public static TraitType byId(String id) {
        for (TraitType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}

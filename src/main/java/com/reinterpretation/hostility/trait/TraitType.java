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
    PLUNDER("plunder", 400, 35, 0xCC1133),
    BERSERKER("berserker", 150, 65, 0xFF3333),
    SEAL("seal", 550, 25, 0x9900CC),
    LIFESTEAL("lifesteal", 120, 75, 0xCC2244),
    ARMOR_PIERCE("armor_pierce", 150, 65, 0xFF9933),
    PROTECTION_PIERCE("protection_pierce", 150, 65, 0x66BBFF),
    STEP_UP("step_up", 50, 90, 0x77DD77),
    UNIVERSAL_RESISTANCE("universal_resistance", 120, 70, 0xAABBCC),
    STURDY("sturdy", 100, 80, 0x778899),
    VITALITY("vitality", 120, 75, 0xFF6699),
    ANTI_REVIVE("anti_revive", 200, 50, 0x552277),
    COUNTERATTACK("counterattack", 100, 75, 0xDDAA22),
    INSPIRE("inspire", 180, 55, 0x44DDDD),
    POISON_STRIKE("poison_strike", 50, 95, 0x55BB33),
    WEAKNESS_STRIKE("weakness_strike", 70, 85, 0x666677),
    RESENTMENT("resentment", 90, 80, 0x881166),
    SWIFT("swift", 50, 90, 0x66EEFF),
    IGNITE("ignite", 50, 90, 0xFF5500),
    PULL("pull", 120, 60, 0x4477FF),
    REPULSE("repulse", 120, 60, 0xFF8844),
    HEAVY("heavy", 60, 85, 0x555566),
    IMMUNITY("immunity", 50, 85, 0xAAFFAA),
    CORROSION("corrosion", 250, 45, 0x779922),
    MENTAL_DISRUPTION("mental_disruption", 170, 55, 0xBB55FF),
    PARALYSIS("paralysis", 170, 55, 0xEEEE44),
    ENDER("ender", 50, 80, 0x9933CC),
    ILLUSION("illusion", 50, 75, 0x88AAFF),
    GUARDIAN_SEAL("guardian_seal", 300, 35, 0xFFD044);

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

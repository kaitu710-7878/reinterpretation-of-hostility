package com.reinterpretation.hostility.capability;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.config.ModConfig;
import dev.xkmc.l2library.capability.player.PlayerCapabilityHolder;
import dev.xkmc.l2library.capability.player.PlayerCapabilityNetworkHandler;
import dev.xkmc.l2library.capability.player.PlayerCapabilityTemplate;
import dev.xkmc.l2serial.serialization.SerialClass;
import dev.xkmc.l2serial.serialization.SerialClass.SerialField;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.ArrayList;

@SerialClass
public class PlayerDifficultyCapability extends PlayerCapabilityTemplate<PlayerDifficultyCapability> {

    public static final Capability<PlayerDifficultyCapability> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static final PlayerCapabilityHolder<PlayerDifficultyCapability> HOLDER =
            new PlayerCapabilityHolder<>(
                    new ResourceLocation(ReinterpretationOfHostility.MODID, "player_difficulty"),
                    CAPABILITY,
                    PlayerDifficultyCapability.class,
                    PlayerDifficultyCapability::new,
                    PlayerCapabilityNetworkHandler::new
            );

    @SerialField
    public boolean unlocked = false;

    @SerialField
    public double dangerPoints = 0.0;

    @SerialField
    public long dangerExp = 0L;

    @SerialField
    public boolean reachedEnd = false;

    @SerialField
    public ArrayList<String> visitedModDimensions = new ArrayList<>();

    public static PlayerDifficultyCapability get(Player player) {
        return HOLDER.get(player);
    }

    public static void register() {
        // Trigger static initialization of HOLDER
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public double getDangerPoints() {
        return dangerPoints;
    }

    public int getDangerLevel() {
        if (!unlocked) {
            return 0;
        }
        double cap = ModConfig.COMMON.maxDangerCap.get();
        return Math.max(0, (int) Math.floor(Math.min(cap, dangerPoints)));
    }

    public long getDangerExp() {
        return dangerExp;
    }

    public long getMaxDangerExp() {
        int lv = getDangerLevel();
        int factor = ModConfig.COMMON.killsPerLevel.get();
        return Math.max(1L, (long) lv * lv * factor);
    }

    public int getEffectiveMaxLevel() {
        int base = ModConfig.COMMON.maxLevel.get();
        int endBonus = reachedEnd ? 10 : 0;
        int modBonus = visitedModDimensions.size() * 5;
        return base + endBonus + modBonus;
    }

    public boolean isReachedEnd() {
        return reachedEnd;
    }

    public int getVisitedModDimensionCount() {
        return visitedModDimensions.size();
    }

    public void unlock(ServerPlayer player) {
        if (!unlocked) {
            unlocked = true;
            HOLDER.network.toClientSyncAll(player);
        }
    }

    public void setDangerPoints(ServerPlayer player, double value) {
        double cap = ModConfig.COMMON.maxDangerCap.get();
        dangerPoints = Mth.clamp(value, 0.0, cap);
        dangerExp = 0L;
        HOLDER.network.toClientSyncAll(player);
    }

    public void addDangerPointsUnchecked(ServerPlayer player, double delta) {
        double cap = ModConfig.COMMON.maxDangerCap.get();
        dangerPoints = Mth.clamp(dangerPoints + delta, 0.0, cap);
        dangerExp = 0L;
        HOLDER.network.toClientSyncAll(player);
    }

    /**
     * L2Hostility-style kill credit growth, defaulting to killsPerLevel = 60 (half the original pace).
     */
    public void addKillCredit(ServerPlayer player, int mobLevel) {
        if (!unlocked) {
            return;
        }
        int dangerCap = (int) Math.floor(ModConfig.COMMON.maxDangerCap.get());
        int current = getDangerLevel();
        if (current >= dangerCap) {
            dangerPoints = dangerCap;
            dangerExp = 0L;
            return;
        }

        int lv = Math.max(1, Math.min(current + 10, mobLevel));
        dangerExp += (long) lv * lv;

        int factor = ModConfig.COMMON.killsPerLevel.get();
        while (current < dangerCap) {
            long req = Math.max(1L, (long) current * current * factor);
            if (dangerExp < req) {
                break;
            }
            dangerExp -= req;
            current++;
        }

        if (current >= dangerCap) {
            current = dangerCap;
            dangerExp = 0L;
        }

        dangerPoints = current;
        HOLDER.network.toClientSyncAll(player);
    }

    public void onDimensionVisited(ServerPlayer player, Level level) {
        boolean changed = false;
        if (level.dimension() == Level.END && !reachedEnd) {
            reachedEnd = true;
            changed = true;
        }
        ResourceLocation dimId = level.dimension().location();
        if (!"minecraft".equals(dimId.getNamespace()) && !visitedModDimensions.contains(dimId.toString())) {
            visitedModDimensions.add(dimId.toString());
            changed = true;
        }
        if (changed) {
            HOLDER.network.toClientSyncAll(player);
        }
    }
}

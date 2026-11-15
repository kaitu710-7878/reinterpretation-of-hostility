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
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

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
        int max = ModConfig.COMMON.maxLevel.get();
        return Math.min(max, Math.max(0, (int) Math.floor(dangerPoints)));
    }

    public void unlock(ServerPlayer player) {
        if (!unlocked) {
            unlocked = true;
            HOLDER.network.toClientSyncAll(player);
        }
    }

    public void addDangerPoints(ServerPlayer player, double amount) {
        if (!unlocked || amount <= 0.0) {
            return;
        }
        dangerPoints += amount;
        HOLDER.network.toClientSyncAll(player);
    }
}

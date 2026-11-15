package com.reinterpretation.hostility;

import com.mojang.logging.LogUtils;
import com.reinterpretation.hostility.capability.EntityDebuffCapability;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.network.NetworkHandler;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ReinterpretationOfHostility.MODID)
public class ReinterpretationOfHostility {

    public static final String MODID = "rohostility";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReinterpretationOfHostility() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        PlayerDifficultyCapability.register();
        MobTraitCapability.register();
        EntityDebuffCapability.register();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
        LOGGER.info("Reinterpretation of Hostility common setup complete.");
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(PlayerDifficultyCapability.class);
        event.register(MobTraitCapability.class);
        event.register(EntityDebuffCapability.class);
    }
}

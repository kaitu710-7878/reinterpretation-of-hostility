package com.reinterpretation.hostility.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.trait.TraitType;
import dev.xkmc.l2tabs.tabs.core.TabRegistry;
import dev.xkmc.l2tabs.tabs.core.TabToken;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;

public final class ClientEvents {

    public static TabToken<TabHostility> TAB_HOSTILITY;

    private ClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = ReinterpretationOfHostility.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                TAB_HOSTILITY = TabRegistry.registerTab(
                        2000,
                        TabHostility::new,
                        () -> Items.WITHER_SKELETON_SKULL,
                        Component.translatable("screen.rohostility.tab.button")
                );
            });
        }
    }

    @Mod.EventBusSubscriber(modid = ReinterpretationOfHostility.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBusEvents {
        @SubscribeEvent
        public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
            if (!ModConfig.COMMON.showMobOverheadInfo.get()) {
                return;
            }
            LivingEntity entity = event.getEntity();
            MobTraitCapability cap = MobTraitCapability.getOrNull(entity);
            if (cap == null || !cap.initialized || cap.level <= 0) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || entity.distanceToSqr(mc.player) > 256.0) {
                return;
            }
            if (mc.crosshairPickEntity != entity) {
                return;
            }

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();

            poseStack.pushPose();
            float height = entity.getBbHeight() + 0.6f;
            poseStack.translate(0.0f, height, 0.0f);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);

            Font font = mc.font;
            Matrix4f matrix = poseStack.last().pose();

            Component lvComp = Component.literal("Lv." + cap.level).withStyle(ChatFormatting.GOLD);
            float lvWidth = -font.width(lvComp) / 2.0f;
            font.drawInBatch(lvComp, lvWidth, 0, 0xFFFFFF, false, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0x40000000, packedLight);

            if (!cap.getTraitIds().isEmpty()) {
                MutableComponent traitsComp = Component.empty();
                boolean first = true;
                for (TraitType type : cap.getTraits()) {
                    if (!first) {
                        traitsComp.append(Component.literal(" ").withStyle(ChatFormatting.WHITE));
                    }
                    traitsComp.append(type.getDisplayName());
                    first = false;
                }
                float trWidth = -font.width(traitsComp) / 2.0f;
                font.drawInBatch(traitsComp, trWidth, -11, 0xFFFFFF, false, matrix, buffer,
                        Font.DisplayMode.NORMAL, 0x40000000, packedLight);
            }

            poseStack.popPose();
        }
    }
}

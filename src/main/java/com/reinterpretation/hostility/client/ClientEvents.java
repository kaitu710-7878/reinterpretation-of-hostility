package com.reinterpretation.hostility.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.trait.TraitType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = ReinterpretationOfHostility.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen inv) {
            int x = inv.getGuiLeft() + inv.getXSize() - 68;
            int y = inv.getGuiTop() - 22;
            if (y < 4) {
                y = 4;
            }
            Button tabBtn = Button.builder(
                            Component.translatable("screen.rohostility.tab.button"),
                            btn -> Minecraft.getInstance().setScreen(new HostilityInfoScreen())
                    )
                    .bounds(x, y, 68, 20)
                    .build();
            event.addListener(tabBtn);
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        if (!ModConfig.COMMON.showMobOverheadInfo.get()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!MobTraitCapability.hasCap(entity)) {
            return;
        }
        MobTraitCapability cap = MobTraitCapability.get(entity);
        if (!cap.initialized || cap.level <= 0) {
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

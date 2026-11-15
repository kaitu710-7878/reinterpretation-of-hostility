package com.reinterpretation.hostility.client;

import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.network.NetworkHandler;
import com.reinterpretation.hostility.network.UnlockHostilityC2SPacket;
import com.reinterpretation.hostility.trait.TraitType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class HostilityInfoScreen extends Screen {

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 230;

    private Button unlockButton;
    private int scrollOffset = 0;

    public HostilityInfoScreen() {
        super(Component.translatable("screen.rohostility.info.title"));
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        Player player = Minecraft.getInstance().player;
        boolean unlocked = player != null && PlayerDifficultyCapability.get(player).isUnlocked();

        if (!unlocked) {
            this.unlockButton = this.addRenderableWidget(
                    Button.builder(
                                    Component.translatable("screen.rohostility.info.unlock_button"),
                                    btn -> {
                                        NetworkHandler.sendToServer(new UnlockHostilityC2SPacket());
                                        btn.active = false;
                                    }
                            )
                            .bounds(left + (PANEL_WIDTH - 200) / 2, top + 115, 200, 20)
                            .build()
            );
        }

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.rohostility.info.back"),
                                btn -> {
                                    Minecraft mc = Minecraft.getInstance();
                                    if (mc.player != null) {
                                        mc.setScreen(new InventoryScreen(mc.player));
                                    } else {
                                        this.onClose();
                                    }
                                }
                        )
                        .bounds(left + PANEL_WIDTH - 70, top + PANEL_HEIGHT - 26, 60, 20)
                        .build()
        );
    }

    @Override
    public void tick() {
        super.tick();
        Player player = Minecraft.getInstance().player;
        if (unlockButton != null && player != null && PlayerDifficultyCapability.get(player).isUnlocked()) {
            this.rebuildWidgets();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xCC181820);
        graphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF666680);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 10, 0xFFFFFF);

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        PlayerDifficultyCapability cap = PlayerDifficultyCapability.get(player);
        if (!cap.isUnlocked()) {
            Component status = Component.translatable("screen.rohostility.info.locked")
                    .withStyle(ChatFormatting.GRAY);
            graphics.drawCenteredString(this.font, status, this.width / 2, top + 40, 0xAAAAAA);

            Component hint1 = Component.translatable("screen.rohostility.info.unlock_hint_1");
            Component hint2 = Component.translatable("screen.rohostility.info.unlock_hint_2")
                    .withStyle(ChatFormatting.RED);
            graphics.drawCenteredString(this.font, hint1, this.width / 2, top + 68, 0xDDDDDD);
            graphics.drawCenteredString(this.font, hint2, this.width / 2, top + 84, 0xFF6666);
        } else {
            int dangerLevel = cap.getDangerLevel();
            int maxLevel = ModConfig.COMMON.maxLevel.get();
            int traitMin = ModConfig.COMMON.traitMinLevel.get();

            Component levelText = Component.translatable(
                    "screen.rohostility.info.danger_level",
                    dangerLevel,
                    maxLevel
            ).withStyle(ChatFormatting.GOLD);
            graphics.drawString(this.font, levelText, left + 16, top + 30, 0xFFFFFF);

            Component pointsText = Component.translatable(
                    "screen.rohostility.info.danger_points",
                    String.format("%.2f", cap.getDangerPoints())
            ).withStyle(ChatFormatting.AQUA);
            graphics.drawString(this.font, pointsText, left + 16, top + 44, 0xFFFFFF);

            double mod = ModConfig.COMMON.statScalingModifier.get();
            double hpMult = 1.0 + dangerLevel * ModConfig.COMMON.healthBonusPerLevel.get() * mod;
            double dmgMult = 1.0 + dangerLevel * ModConfig.COMMON.damageBonusPerLevel.get() * mod;
            Component scalingText = Component.translatable(
                    "screen.rohostility.info.scaling",
                    String.format("%.2fx", hpMult),
                    String.format("%.2fx", dmgMult)
            ).withStyle(ChatFormatting.GREEN);
            graphics.drawString(this.font, scalingText, left + 16, top + 58, 0xFFFFFF);

            Component traitHeader = Component.translatable(
                    "screen.rohostility.info.traits_header",
                    traitMin
            ).withStyle(dangerLevel >= traitMin ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY);
            graphics.drawString(this.font, traitHeader, left + 16, top + 76, 0xFFFFFF);

            int listTop = top + 92;
            int listBottom = top + PANEL_HEIGHT - 32;
            int maxVisible = Math.max(1, (listBottom - listTop) / 12);

            List<TraitType> enabledTraits = new ArrayList<>();
            for (TraitType type : TraitType.values()) {
                if (type.isEnabled()) {
                    enabledTraits.add(type);
                }
            }

            int maxScroll = Math.max(0, enabledTraits.size() - maxVisible);
            scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

            int y = listTop;
            for (int i = scrollOffset; i < Math.min(enabledTraits.size(), scrollOffset + maxVisible); i++) {
                TraitType type = enabledTraits.get(i);
                Component line = Component.literal("• ")
                        .append(type.getDisplayName())
                        .append(Component.literal(" (Cost " + type.getCost() + ") - ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(type.getDescription().withStyle(ChatFormatting.GRAY));
                graphics.drawString(this.font, line, left + 18, y, 0xFFFFFF);
                y += 12;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            return true;
        } else if (delta < 0) {
            scrollOffset++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

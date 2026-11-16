package com.reinterpretation.hostility.client;

import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.network.NetworkHandler;
import com.reinterpretation.hostility.network.UnlockHostilityC2SPacket;
import com.reinterpretation.hostility.trait.TraitType;
import dev.xkmc.l2tabs.tabs.contents.BaseTextScreen;
import dev.xkmc.l2tabs.tabs.core.TabManager;
import dev.xkmc.l2tabs.tabs.core.TabToken;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HostilityInfoScreen extends BaseTextScreen {

    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation("l2tabs", "textures/gui/empty.png");

    private final TabToken<TabHostility> tabToken;
    private TabManager tabManager;
    private Button unlockButton;
    private int scrollOffset = 0;

    public HostilityInfoScreen(TabToken<TabHostility> tabToken) {
        super(Component.translatable("screen.rohostility.info.title"), BG_TEXTURE);
        this.tabToken = tabToken;
    }

    @Override
    public void init() {
        super.init();
        this.tabManager = new TabManager(this);
        this.tabManager.init(this::addRenderableWidget, this.tabToken);

        Player player = Minecraft.getInstance().player;
        boolean unlocked = player != null && PlayerDifficultyCapability.get(player).isUnlocked();
        this.unlockButton = null;

        if (!unlocked) {
            this.unlockButton = this.addRenderableWidget(
                    Button.builder(
                                    Component.translatable("screen.rohostility.info.unlock_button"),
                                    btn -> {
                                        NetworkHandler.sendToServer(new UnlockHostilityC2SPacket());
                                        btn.active = false;
                                    }
                            )
                            .bounds(this.leftPos + (this.imageWidth - 154) / 2, this.topPos + 92, 154, 20)
                            .build()
            );
        }
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
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = this.leftPos + 10;
        int y = this.topPos + 10;
        int maxW = this.imageWidth - 20;
        graphics.drawString(this.font, this.font.plainSubstrByWidth(this.title.getString(), maxW), x, y, 0x404040, false);

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            if (this.tabManager != null) {
                this.tabManager.onToolTipRender(graphics, mouseX, mouseY);
            }
            return;
        }

        PlayerDifficultyCapability cap = PlayerDifficultyCapability.get(player);
        if (!cap.isUnlocked()) {
            Component status = Component.translatable("screen.rohostility.info.locked")
                    .withStyle(ChatFormatting.DARK_GRAY);
            graphics.drawWordWrap(this.font, status, x, y + 18, this.imageWidth - 20, 0x404040);

            Component hint1 = Component.translatable("screen.rohostility.info.unlock_hint_1");
            graphics.drawWordWrap(this.font, hint1, x, y + 36, this.imageWidth - 20, 0x404040);

            Component hint2 = Component.translatable("screen.rohostility.info.unlock_hint_2")
                    .withStyle(ChatFormatting.DARK_RED);
            graphics.drawWordWrap(this.font, hint2, x, y + 60, this.imageWidth - 20, 0xAA0000);
        } else {
            double danger = cap.getDangerPoints();
            double dangerCap = ModConfig.COMMON.maxDangerCap.get();
            int effectiveMax = cap.getEffectiveMaxLevel();

            int minLv = Mth.clamp((int) Math.round(danger * 0.85) - ModConfig.COMMON.levelRandomVariance.get(), 0, effectiveMax);
            int maxLv = Mth.clamp((int) Math.round(danger * 1.15) + ModConfig.COMMON.levelRandomVariance.get(), 0, effectiveMax);

            Component dangerText = Component.translatable(
                    "screen.rohostility.info.danger_points_cap",
                    String.format("%.0f", danger),
                    String.format("%.0f", dangerCap),
                    cap.getDangerExp(),
                    cap.getMaxDangerExp()
            );
            graphics.drawString(this.font, this.font.plainSubstrByWidth(dangerText.getString(), maxW), x, y + 14, 0x8B0000, false);

            Component rangeText = Component.translatable(
                    "screen.rohostility.info.level_range",
                    minLv, maxLv
            );
            graphics.drawString(this.font, this.font.plainSubstrByWidth(rangeText.getString(), maxW), x, y + 25, 0x202020, false);

            Component maxLevelText = Component.translatable(
                    "screen.rohostility.info.max_level_cap",
                    effectiveMax,
                    cap.isReachedEnd() ? 10 : 0,
                    cap.getVisitedModDimensionCount() * 5
            );
            graphics.drawString(this.font, this.font.plainSubstrByWidth(maxLevelText.getString(), maxW), x, y + 36, 0x004488, false);

            int traitMin = ModConfig.COMMON.traitMinLevel.get();
            Component traitHeader = Component.translatable(
                    "screen.rohostility.info.traits_header",
                    traitMin
            );
            graphics.drawString(this.font, this.font.plainSubstrByWidth(traitHeader.getString(), maxW), x, y + 49, 0x404040, false);

            List<TraitType> sortedTraits = new ArrayList<>();
            for (TraitType type : TraitType.values()) {
                if (type.isEnabled()) {
                    sortedTraits.add(type);
                }
            }
            sortedTraits.sort(Comparator.comparingInt(TraitType::getCost).thenComparing(TraitType::getId));

            int listTop = y + 61;
            int listBottom = this.topPos + this.imageHeight - 10;
            int maxVisible = Math.max(1, (listBottom - listTop) / 10);
            int maxScroll = Math.max(0, sortedTraits.size() - maxVisible);
            scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

            int rowY = listTop;
            for (int i = scrollOffset; i < Math.min(sortedTraits.size(), scrollOffset + maxVisible); i++) {
                TraitType type = sortedTraits.get(i);
                Component line = Component.literal("[" + type.getCost() + "] ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(type.getDisplayName());
                graphics.drawString(this.font, line, x, rowY, 0xFFFFFF, false);

                if (mouseX >= x && mouseX <= x + this.imageWidth - 20 && mouseY >= rowY && mouseY < rowY + 10) {
                    graphics.renderTooltip(this.font, type.getDescription(), mouseX, mouseY);
                }
                rowY += 10;
            }
        }

        if (this.tabManager != null) {
            this.tabManager.onToolTipRender(graphics, mouseX, mouseY);
        }
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
}

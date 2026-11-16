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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

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
    private int maxScroll = 0;

    private record DisplayLine(FormattedCharSequence text, int color, @Nullable TraitType trait) {
    }

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
                            .bounds(this.leftPos + (this.imageWidth - 154) / 2, this.topPos + 110, 154, 20)
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
        int y = this.topPos + 8;
        int maxW = this.imageWidth - 22;

        List<FormattedCharSequence> titleLines = this.font.split(this.title, maxW);
        for (FormattedCharSequence line : titleLines) {
            graphics.drawString(this.font, line, x, y, 0x404040, false);
            y += 10;
        }
        y += 2;

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
            graphics.drawWordWrap(this.font, status, x, y, maxW, 0x404040);
            y += this.font.split(status, maxW).size() * 10 + 4;

            Component hint1 = Component.translatable("screen.rohostility.info.unlock_hint_1");
            graphics.drawWordWrap(this.font, hint1, x, y, maxW, 0x404040);
            y += this.font.split(hint1, maxW).size() * 10 + 4;

            Component hint2 = Component.translatable("screen.rohostility.info.unlock_hint_2")
                    .withStyle(ChatFormatting.DARK_RED);
            graphics.drawWordWrap(this.font, hint2, x, y, maxW, 0xAA0000);
        } else {
            List<DisplayLine> lines = buildUnlockedLines(cap, maxW);
            int contentBottom = this.topPos + this.imageHeight - 10;
            int lineHeight = 10;
            int maxVisible = Math.max(1, (contentBottom - y) / lineHeight);
            this.maxScroll = Math.max(0, lines.size() - maxVisible);
            this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);

            TraitType hoveredTrait = null;
            int rowY = y;
            for (int i = scrollOffset; i < Math.min(lines.size(), scrollOffset + maxVisible); i++) {
                DisplayLine entry = lines.get(i);
                graphics.drawString(this.font, entry.text(), x, rowY, entry.color(), false);
                if (entry.trait() != null && mouseX >= x && mouseX <= x + maxW && mouseY >= rowY && mouseY < rowY + lineHeight) {
                    hoveredTrait = entry.trait();
                }
                rowY += lineHeight;
            }

            if (this.maxScroll > 0) {
                int barX = this.leftPos + this.imageWidth - 9;
                int barTop = y;
                int barH = contentBottom - y;
                graphics.fill(barX, barTop, barX + 3, barTop + barH, 0x40000000);
                int thumbH = Math.max(10, barH * maxVisible / lines.size());
                int thumbY = barTop + (barH - thumbH) * scrollOffset / this.maxScroll;
                graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFF606070);
            }

            if (hoveredTrait != null) {
                graphics.renderTooltip(this.font, hoveredTrait.getDescription(), mouseX, mouseY);
            }
        }

        if (this.tabManager != null) {
            this.tabManager.onToolTipRender(graphics, mouseX, mouseY);
        }
    }

    private List<DisplayLine> buildUnlockedLines(PlayerDifficultyCapability cap, int maxW) {
        List<DisplayLine> lines = new ArrayList<>();
        double danger = cap.getDangerPoints();
        double dangerCap = ModConfig.COMMON.maxDangerCap.get();
        int effectiveMax = cap.getEffectiveMaxLevel();

        int minLv = Mth.clamp((int) Math.round(danger * 0.85) - ModConfig.COMMON.levelRandomVariance.get(), 0, effectiveMax);
        int maxLv = Mth.clamp((int) Math.round(danger * 1.15) + ModConfig.COMMON.levelRandomVariance.get(), 0, effectiveMax);

        addWrapped(lines, Component.translatable(
                "screen.rohostility.info.danger_points_cap",
                String.format("%.0f", danger),
                String.format("%.0f", dangerCap),
                cap.getDangerExp(),
                cap.getMaxDangerExp()
        ), maxW, 0x8B0000, null);

        addWrapped(lines, Component.translatable(
                "screen.rohostility.info.level_range",
                minLv, maxLv
        ), maxW, 0x202020, null);

        addWrapped(lines, Component.translatable(
                "screen.rohostility.info.max_level_cap",
                effectiveMax,
                cap.isReachedEnd() ? 10 : 0,
                cap.getVisitedModDimensionCount() * 5
        ), maxW, 0x004488, null);

        addWrapped(lines, Component.translatable(
                "screen.rohostility.info.traits_header"
        ), maxW, 0x404040, null);

        List<TraitType> sortedTraits = new ArrayList<>();
        for (TraitType type : TraitType.values()) {
            if (type.isEnabled()) {
                sortedTraits.add(type);
            }
        }
        sortedTraits.sort(Comparator.comparingInt(TraitType::getCost).thenComparing(TraitType::getId));

        for (TraitType type : sortedTraits) {
            Component traitComp = Component.literal("[" + type.getCost() + "] ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(type.getDisplayName());
            addWrapped(lines, traitComp, maxW, 0xFFFFFF, type);
        }

        return lines;
    }

    private void addWrapped(List<DisplayLine> out, Component comp, int maxW, int color, @Nullable TraitType trait) {
        for (FormattedCharSequence seq : this.font.split(comp, maxW)) {
            out.add(new DisplayLine(seq, color, trait));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            return true;
        } else if (delta < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}

package com.reinterpretation.hostility.client;

import com.reinterpretation.hostility.item.DebugMobEditorItem;
import com.reinterpretation.hostility.network.NetworkHandler;
import com.reinterpretation.hostility.network.SaveDebugEditorC2SPacket;
import com.reinterpretation.hostility.trait.TraitType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class DebugMobEditorScreen extends Screen {

    private static final int MAX_PANEL_WIDTH = 380;
    private static final int MAX_PANEL_HEIGHT = 260;
    private static final int ROW_HEIGHT = 21;

    private final InteractionHand hand;
    private final int initialLevel;
    private final Set<String> selectedTraits = new LinkedHashSet<>();

    private EditBox levelBox;
    private final List<TraitButtonEntry> traitButtons = new ArrayList<>();

    private int panelW;
    private int panelH;
    private int left;
    private int top;
    private int listTop;
    private int listBottom;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private record TraitButtonEntry(TraitType type, Button button, int baseY) {
    }

    public DebugMobEditorScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("screen.rohostility.debug_editor.title"));
        this.hand = hand;
        this.selectedTraits.addAll(DebugMobEditorItem.getTraits(stack));
        this.initialLevel = DebugMobEditorItem.getLevel(stack);
    }

    public static void open(InteractionHand hand, ItemStack stack) {
        Minecraft.getInstance().setScreen(new DebugMobEditorScreen(hand, stack));
    }

    @Override
    protected void init() {
        super.init();
        this.traitButtons.clear();

        this.panelW = Math.min(MAX_PANEL_WIDTH, Math.max(220, this.width - 16));
        this.panelH = Math.min(MAX_PANEL_HEIGHT, Math.max(140, this.height - 16));
        this.left = (this.width - panelW) / 2;
        this.top = (this.height - panelH) / 2;
        this.listTop = top + 46;
        this.listBottom = top + panelH - 30;

        String prevLevelText = this.levelBox != null ? this.levelBox.getValue() : String.valueOf(initialLevel);
        this.levelBox = new EditBox(this.font, left + 90, top + 22, Math.min(100, panelW - 104), 16,
                Component.translatable("screen.rohostility.debug_editor.level"));
        this.levelBox.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.levelBox.setValue(prevLevelText);
        this.addRenderableWidget(this.levelBox);

        List<TraitType> sorted = new ArrayList<>(Arrays.asList(TraitType.values()));
        sorted.sort(Comparator.comparingInt(TraitType::getCost).thenComparing(TraitType::getId));

        int cols = panelW >= 320 ? 3 : 2;
        int innerW = panelW - 28; // 12px left margin + 16px right margin (scrollbar space)
        int gap = 5;
        int btnW = (innerW - gap * (cols - 1)) / cols;
        int btnH = 18;
        int startX = left + 12;

        for (int i = 0; i < sorted.size(); i++) {
            TraitType type = sorted.get(i);
            int col = i % cols;
            int row = i / cols;
            int bx = startX + col * (btnW + gap);
            int baseY = listTop + row * ROW_HEIGHT;

            Button toggle = Button.builder(
                            formatTraitButton(type),
                            btn -> {
                                if (selectedTraits.contains(type.getId())) {
                                    selectedTraits.remove(type.getId());
                                } else {
                                    selectedTraits.add(type.getId());
                                }
                                btn.setMessage(formatTraitButton(type));
                            }
                    )
                    .bounds(bx, baseY, btnW, btnH)
                    .build();
            this.traitButtons.add(new TraitButtonEntry(type, toggle, baseY));
            this.addWidget(toggle);
        }

        int totalRows = (sorted.size() + cols - 1) / cols;
        int contentHeight = totalRows * ROW_HEIGHT;
        int viewHeight = Math.max(1, listBottom - listTop);
        this.maxScroll = Math.max(0, contentHeight - viewHeight);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);
        updateTraitButtonPositions();

        int footerBtnW = Math.min(65, (panelW - 36) / 3);
        int footerY = top + panelH - 24;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.rohostility.debug_editor.clear"),
                                btn -> {
                                    selectedTraits.clear();
                                    for (TraitButtonEntry entry : traitButtons) {
                                        entry.button().setMessage(formatTraitButton(entry.type()));
                                    }
                                }
                        )
                        .bounds(left + 12, footerY, footerBtnW, 18)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.rohostility.debug_editor.save"),
                                btn -> {
                                    int lv = 0;
                                    try {
                                        if (!levelBox.getValue().isEmpty()) {
                                            lv = Integer.parseInt(levelBox.getValue());
                                        }
                                    } catch (NumberFormatException ignored) {
                                    }
                                    NetworkHandler.sendToServer(new SaveDebugEditorC2SPacket(
                                            hand, lv, new ArrayList<>(selectedTraits)
                                    ));
                                    this.onClose();
                                }
                        )
                        .bounds(left + panelW - (footerBtnW * 2 + 18), footerY, footerBtnW, 18)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.rohostility.info.back"),
                                btn -> this.onClose()
                        )
                        .bounds(left + panelW - (footerBtnW + 12), footerY, footerBtnW, 18)
                        .build()
        );
    }

    private void updateTraitButtonPositions() {
        for (TraitButtonEntry entry : traitButtons) {
            int y = entry.baseY() - scrollOffset;
            Button btn = entry.button();
            btn.setY(y);
            boolean visible = (y + btn.getHeight() > listTop) && (y < listBottom);
            btn.visible = visible;
            btn.active = visible;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScroll > 0 && mouseX >= left && mouseX <= left + panelW && mouseY >= listTop && mouseY <= listBottom) {
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta) * ROW_HEIGHT, 0, maxScroll);
            updateTraitButtonPositions();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Ignore clicks on clipped parts of partially visible buttons outside the scroll viewport
        if (mouseY < listTop || mouseY >= listBottom) {
            for (TraitButtonEntry entry : traitButtons) {
                entry.button().active = false;
            }
            boolean handled = super.mouseClicked(mouseX, mouseY, button);
            updateTraitButtonPositions();
            return handled;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private Component formatTraitButton(TraitType type) {
        boolean on = selectedTraits.contains(type.getId());
        return Component.literal(on ? "[✓] " : "[ ] ")
                .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                .append(Component.translatable(type.getTranslationKey()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.fill(left, top, left + panelW, top + panelH, 0xDD1A1A24);
        graphics.renderOutline(left, top, panelW, panelH, 0xFF777799);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 8, 0xFFFFFF);
        graphics.drawString(this.font,
                Component.translatable("screen.rohostility.debug_editor.level"),
                left + 14, top + 26, 0xDDDDDD, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.enableScissor(left + 8, listTop, left + panelW - 8, listBottom);
        int clampedMouseY = (mouseY >= listTop && mouseY < listBottom) ? mouseY : -1;
        for (TraitButtonEntry entry : traitButtons) {
            if (entry.button().visible) {
                entry.button().render(graphics, mouseX, clampedMouseY, partialTick);
            }
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = left + panelW - 10;
            int barH = Math.max(12, listBottom - listTop);
            graphics.fill(barX, listTop, barX + 4, listBottom, 0x44FFFFFF);
            int thumbH = Math.max(16, (barH * barH) / (barH + maxScroll));
            int thumbY = listTop + (int) ((barH - thumbH) * ((double) scrollOffset / maxScroll));
            graphics.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xCCBBBBDD);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

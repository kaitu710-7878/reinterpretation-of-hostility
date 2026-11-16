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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class DebugMobEditorScreen extends Screen {

    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 246;

    private final InteractionHand hand;
    private EditBox levelBox;
    private final Set<String> selectedTraits = new LinkedHashSet<>();

    public DebugMobEditorScreen(InteractionHand hand, ItemStack stack) {
        super(Component.translatable("screen.rohostility.debug_editor.title"));
        this.hand = hand;
        this.selectedTraits.addAll(DebugMobEditorItem.getTraits(stack));
        this.initialLevel = DebugMobEditorItem.getLevel(stack);
    }

    private final int initialLevel;

    public static void open(InteractionHand hand, ItemStack stack) {
        Minecraft.getInstance().setScreen(new DebugMobEditorScreen(hand, stack));
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        this.levelBox = new EditBox(this.font, left + 90, top + 24, 80, 16,
                Component.translatable("screen.rohostility.debug_editor.level"));
        this.levelBox.setFilter(s -> s.isEmpty() || s.chars().allMatch(Character::isDigit));
        this.levelBox.setValue(String.valueOf(initialLevel));
        this.addRenderableWidget(this.levelBox);

        List<TraitType> sorted = new ArrayList<>(Arrays.asList(TraitType.values()));
        sorted.sort(Comparator.comparingInt(TraitType::getCost).thenComparing(TraitType::getId));

        int cols = 3;
        int btnW = 114;
        int btnH = 18;
        int startX = left + 12;
        int startY = top + 48;

        for (int i = 0; i < sorted.size(); i++) {
            TraitType type = sorted.get(i);
            int col = i % cols;
            int row = i / cols;
            int bx = startX + col * (btnW + 6);
            int by = startY + row * (btnH + 3);

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
                    .bounds(bx, by, btnW, btnH)
                    .build();
            this.addRenderableWidget(toggle);
        }

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
                        .bounds(left + PANEL_WIDTH - 150, top + PANEL_HEIGHT - 24, 65, 18)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.rohostility.debug_editor.clear"),
                                btn -> {
                                    selectedTraits.clear();
                                    this.rebuildWidgets();
                                }
                        )
                        .bounds(left + 12, top + PANEL_HEIGHT - 24, 65, 18)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.rohostility.info.back"),
                                btn -> this.onClose()
                        )
                        .bounds(left + PANEL_WIDTH - 78, top + PANEL_HEIGHT - 24, 65, 18)
                        .build()
        );
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
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xDD1A1A24);
        graphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF777799);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 8, 0xFFFFFF);
        graphics.drawString(this.font,
                Component.translatable("screen.rohostility.debug_editor.level"),
                left + 14, top + 28, 0xDDDDDD, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

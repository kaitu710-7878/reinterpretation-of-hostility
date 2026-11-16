package com.reinterpretation.hostility.client;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.EntityDebuffCapability;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.compat.CuriosCompat;
import com.reinterpretation.hostility.config.ModConfig;
import com.reinterpretation.hostility.trait.TraitType;
import com.reinterpretation.hostility.trait.UnrealityEnchantHelper;
import dev.xkmc.l2tabs.tabs.core.TabRegistry;
import dev.xkmc.l2tabs.tabs.core.TabToken;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Matrix4f;

import java.util.Map;

public final class ClientEvents {

    public static TabToken<TabHostility> TAB_HOSTILITY;
    private static final boolean NEAT_LOADED = ModList.get().isLoaded("neat");

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
        public static void onItemTooltip(ItemTooltipEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            ItemStack stack = event.getItemStack();
            long gameTime = mc.level.getGameTime();

            UnrealityEnchantHelper.modifyTooltip(stack, event.getToolTip(), gameTime);

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(EntityDebuffCapability.TAG_APOCALYPSE_EXPIRE)) {
                long expire = tag.getLong(EntityDebuffCapability.TAG_APOCALYPSE_EXPIRE);
                if (gameTime < expire) {
                    long remSec = Math.max(1L, (expire - gameTime + 19L) / 20L);
                    event.getToolTip().add(Component.translatable(
                            "tooltip.rohostility.apocalypse_weakened", remSec
                    ).withStyle(ChatFormatting.RED));

                    EquipmentSlot slot = stack.getEquipmentSlot();
                    if (slot == null && stack.getItem() instanceof ArmorItem armor) {
                        slot = armor.getEquipmentSlot();
                    }
                    if (slot != null && slot.getType() == EquipmentSlot.Type.ARMOR) {
                        Multimap<Attribute, AttributeModifier> mods = stack.getAttributeModifiers(slot);
                        for (Map.Entry<Attribute, AttributeModifier> entry : mods.entries()) {
                            AttributeModifier mod = entry.getValue();
                            if (mod.getAmount() <= 0.0) {
                                continue;
                            }
                            double reduced = mod.getAmount() * 0.9;
                            String valStr = switch (mod.getOperation()) {
                                case ADDITION -> String.format("+%.2f", reduced);
                                case MULTIPLY_BASE, MULTIPLY_TOTAL -> String.format("+%.1f%%", reduced * 100.0);
                            };
                            event.getToolTip().add(Component.literal("  ")
                                    .append(Component.translatable(entry.getKey().getDescriptionId()))
                                    .append(Component.literal(": " + valStr + " (-10%)"))
                                    .withStyle(ChatFormatting.DARK_RED));
                        }
                    } else if (CuriosCompat.isLoaded() && mc.player != null) {
                        for (CuriosCompat.CurioSlotEntry curio : CuriosCompat.getEquippedCurios(mc.player)) {
                            if (curio.stack() == stack) {
                                String rest = curio.slotKey().substring("curios:".length());
                                int lastColon = rest.lastIndexOf(':');
                                if (lastColon > 0) {
                                    String slotId = rest.substring(0, lastColon);
                                    int idx = Integer.parseInt(rest.substring(lastColon + 1));
                                    Multimap<Attribute, AttributeModifier> mods =
                                            CuriosCompat.getCurioAttributeModifiers(mc.player, slotId, idx, stack);
                                    for (Map.Entry<Attribute, AttributeModifier> entry : mods.entries()) {
                                        AttributeModifier mod = entry.getValue();
                                        if (mod.getAmount() <= 0.0) {
                                            continue;
                                        }
                                        double reduced = mod.getAmount() * 0.9;
                                        String valStr = switch (mod.getOperation()) {
                                            case ADDITION -> String.format("+%.2f", reduced);
                                            case MULTIPLY_BASE, MULTIPLY_TOTAL -> String.format("+%.1f%%", reduced * 100.0);
                                        };
                                        event.getToolTip().add(Component.literal("  ")
                                                .append(Component.translatable(entry.getKey().getDescriptionId()))
                                                .append(Component.literal(": " + valStr + " (-10%)"))
                                                .withStyle(ChatFormatting.DARK_RED));
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

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
            if (mc.player == null || entity.distanceToSqr(mc.player) > 1024.0) {
                return;
            }
            if (entity.isInvisible() && mc.crosshairPickEntity != entity) {
                return;
            }
            if (!mc.player.hasLineOfSight(entity)) {
                return;
            }

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            int packedLight = event.getPackedLight();

            poseStack.pushPose();
            float height = entity.getBbHeight() + 0.65f + (NEAT_LOADED ? 0.55f : 0.0f);
            poseStack.translate(0.0f, height, 0.0f);
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(-0.025f, -0.025f, 0.025f);

            Font font = mc.font;
            Matrix4f matrix = poseStack.last().pose();
            Font.DisplayMode displayMode = Font.DisplayMode.SEE_THROUGH;

            Component lvComp = Component.literal("Lv." + cap.level).withStyle(ChatFormatting.GOLD);
            float lvWidth = -font.width(lvComp) / 2.0f;
            font.drawInBatch(lvComp, lvWidth, 0, 0xFFFFFF, false, matrix, buffer,
                    displayMode, 0x40000000, packedLight);

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
                        displayMode, 0x40000000, packedLight);
            }

            poseStack.popPose();
        }
    }
}

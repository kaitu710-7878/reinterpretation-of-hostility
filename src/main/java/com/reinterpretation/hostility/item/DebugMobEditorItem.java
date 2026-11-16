package com.reinterpretation.hostility.item;

import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.client.DebugMobEditorScreen;
import com.reinterpretation.hostility.logic.HostilityScaler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DebugMobEditorItem extends Item {

    private static final String TAG_LEVEL = "debug_level";
    private static final String TAG_TRAITS = "debug_traits";

    public DebugMobEditorItem() {
        super(new Properties().stacksTo(1));
    }

    public static int getLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? Math.max(0, tag.getInt(TAG_LEVEL)) : 0;
    }

    public static List<String> getTraits(ItemStack stack) {
        List<String> list = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_TRAITS, Tag.TAG_LIST)) {
            ListTag nbtList = tag.getList(TAG_TRAITS, Tag.TAG_STRING);
            for (int i = 0; i < nbtList.size(); i++) {
                list.add(nbtList.getString(i));
            }
        }
        return list;
    }

    public static void saveConfig(ItemStack stack, int level, List<String> traitIds) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_LEVEL, Math.max(0, level));
        ListTag list = new ListTag();
        for (String id : traitIds) {
            list.add(StringTag.valueOf(id));
        }
        tag.put(TAG_TRAITS, list);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DebugMobEditorScreen.open(hand, stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)
                || !(target instanceof Mob)
                || !MobTraitCapability.isHostileCandidate(target)) {
            return InteractionResult.PASS;
        }

        int targetLevel = getLevel(stack);
        List<String> targetTraits = getTraits(stack);

        Entity created = target.getType().create(serverLevel);
        if (!(created instanceof Mob newMob)) {
            return InteractionResult.FAIL;
        }

        newMob.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());

        MobTraitCapability newCap = MobTraitCapability.get(newMob);
        newCap.initialized = true;
        newCap.level = targetLevel;
        newCap.traitIds.clear();
        newCap.traitIds.addAll(targetTraits);

        // Respawn through finalizeSpawn so external mods recalculate their spawn buffs/scaling
        newMob.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(target.blockPosition()),
                MobSpawnType.COMMAND,
                null,
                null
        );

        serverLevel.addFreshEntity(newMob);

        if (targetLevel > 0) {
            HostilityScaler.applyLevelStats(newMob, targetLevel);
        }
        HostilityScaler.applyTraitSpawnAttributes(newMob, newCap);
        HostilityScaler.syncToTracking(newMob, newCap);

        target.discard();

        player.displayClientMessage(
                Component.translatable("item.rohostility.debug_mob_editor.applied",
                        targetLevel, targetTraits.size()),
                true
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.rohostility.debug_mob_editor.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.rohostility.debug_mob_editor.info",
                getLevel(stack), getTraits(stack).size()).withStyle(ChatFormatting.AQUA));
    }
}

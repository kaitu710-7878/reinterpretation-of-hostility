package com.reinterpretation.hostility.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.capability.PlayerDifficultyCapability;
import com.reinterpretation.hostility.logic.HostilityScaler;
import com.reinterpretation.hostility.trait.TraitType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Collection;

@Mod.EventBusSubscriber(modid = ReinterpretationOfHostility.MODID)
public final class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> TRAIT_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    Arrays.stream(TraitType.values()).map(TraitType::getId), builder);

    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rohostility")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("unlock")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> {
                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                    for (ServerPlayer player : players) {
                                        PlayerDifficultyCapability.get(player).unlock(player);
                                    }
                                    ctx.getSource().sendSuccess(() ->
                                            Component.translatable("command.rohostility.unlock", players.size()), true);
                                    return players.size();
                                })))
                .then(Commands.literal("danger")
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    double val = DoubleArgumentType.getDouble(ctx, "value");
                                                    for (ServerPlayer player : players) {
                                                        PlayerDifficultyCapability.get(player).setDangerPoints(player, val);
                                                    }
                                                    ctx.getSource().sendSuccess(() ->
                                                            Component.translatable("command.rohostility.danger.set",
                                                                    String.format("%.1f", val), players.size()), true);
                                                    return players.size();
                                                }))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("delta", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                                    double delta = DoubleArgumentType.getDouble(ctx, "delta");
                                                    for (ServerPlayer player : players) {
                                                        PlayerDifficultyCapability.get(player).addDangerPointsUnchecked(player, delta);
                                                    }
                                                    ctx.getSource().sendSuccess(() ->
                                                            Component.translatable("command.rohostility.danger.add",
                                                                    String.format("%+.1f", delta), players.size()), true);
                                                    return players.size();
                                                })))))
                .then(Commands.literal("trait")
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.argument("trait", StringArgumentType.word())
                                                .suggests(TRAIT_SUGGESTIONS)
                                                .executes(ctx -> {
                                                    Collection<? extends Entity> entities = EntityArgument.getEntities(ctx, "targets");
                                                    String traitId = StringArgumentType.getString(ctx, "trait");
                                                    TraitType trait = TraitType.byId(traitId);
                                                    if (trait == null) {
                                                        ctx.getSource().sendFailure(
                                                                Component.translatable("command.rohostility.trait.unknown", traitId));
                                                        return 0;
                                                    }
                                                    int count = 0;
                                                    for (Entity entity : entities) {
                                                        if (entity instanceof LivingEntity living && MobTraitCapability.hasCap(living)) {
                                                            MobTraitCapability cap = MobTraitCapability.get(living);
                                                            cap.initialized = true;
                                                            cap.addTrait(trait);
                                                            HostilityScaler.applyTraitSpawnAttributes(living, cap);
                                                            HostilityScaler.syncToTracking(living, cap);
                                                            count++;
                                                        }
                                                    }
                                                    int finalCount = count;
                                                    ctx.getSource().sendSuccess(() ->
                                                            Component.translatable("command.rohostility.trait.add",
                                                                    trait.getDisplayName(), finalCount), true);
                                                    return finalCount;
                                                }))))));
    }
}

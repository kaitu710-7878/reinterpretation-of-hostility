package com.reinterpretation.hostility.compat.jade;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import com.reinterpretation.hostility.capability.MobTraitCapability;
import com.reinterpretation.hostility.trait.TraitType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class RoHostilityJadePlugin implements IWailaPlugin {

    public static final ResourceLocation UID =
            new ResourceLocation(ReinterpretationOfHostility.MODID, "mob_info");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(Provider.INSTANCE, Mob.class);
    }

    private enum Provider implements IEntityComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getEntity() instanceof LivingEntity living)) {
                return;
            }
            MobTraitCapability cap = MobTraitCapability.getOrNull(living);
            if (cap == null || !cap.initialized || cap.level <= 0) {
                return;
            }

            tooltip.add(Component.translatable("jade.rohostility.level", cap.level)
                    .withStyle(ChatFormatting.GOLD));

            if (!cap.getTraitIds().isEmpty()) {
                MutableComponent traitsLine = Component.empty();
                boolean first = true;
                for (TraitType type : cap.getTraits()) {
                    if (!first) {
                        traitsLine.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    }
                    traitsLine.append(type.getDisplayName());
                    first = false;
                }
                tooltip.add(traitsLine);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}

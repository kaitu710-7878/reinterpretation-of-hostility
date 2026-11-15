package com.reinterpretation.hostility.item;

import com.reinterpretation.hostility.ReinterpretationOfHostility;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReinterpretationOfHostility.MODID);

    public static final RegistryObject<Item> SEALED_ITEM =
            ITEMS.register("sealed_item", SealedItem::new);

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}

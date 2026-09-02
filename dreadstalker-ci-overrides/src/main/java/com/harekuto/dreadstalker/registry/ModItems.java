package com.harekuto.dreadstalker.registry;

import com.harekuto.dreadstalker.DreadstalkerMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DreadstalkerMod.MODID);
    public static final RegistryObject<Item> DREADSTALKER_SPAWN_EGG = ITEMS.register("dreadstalker_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DREADSTALKER.get(), 0x171013, 0xFF1B16, new Item.Properties()));

    private ModItems() {}
}

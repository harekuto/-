package com.harekuto.dreadstalker.registry;

import com.harekuto.dreadstalker.DreadstalkerMod;
import com.harekuto.dreadstalker.item.DreadEyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DreadstalkerMod.MODID);

    public static final RegistryObject<Item> DREADSTALKER_SPAWN_EGG = ITEMS.register("dreadstalker_spawn_egg",
            () -> new SpawnEggItem(ModEntities.DREADSTALKER.get(), 0x171013, 0xFF1B16, new Item.Properties()));

    public static final RegistryObject<Item> DREAD_EYE = ITEMS.register("dread_eye",
            () -> new DreadEyeItem(new Item.Properties().stacksTo(1).durability(16)));

    private ModItems() {}
}

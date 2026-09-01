package net.execheinz.upgrader.registry;

import net.execheinz.upgrader.Upgrader;
import net.execheinz.upgrader.item.UpgraderItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Upgrader.MODID);
    public static final RegistryObject<Item> UPGRADER = ITEMS.register("station",
        () -> new UpgraderItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** Internal client preview item used only to render bundled Musor Drop case models. */
    public static final RegistryObject<Item> CASE_DISPLAY = ITEMS.register("case_display",
        () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) { ITEMS.register(bus); }
    private ModItems() {}
}

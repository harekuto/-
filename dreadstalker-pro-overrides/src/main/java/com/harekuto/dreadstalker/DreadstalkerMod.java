package com.harekuto.dreadstalker;

import com.harekuto.dreadstalker.config.DreadstalkerConfig;
import com.harekuto.dreadstalker.registry.ModEntities;
import com.harekuto.dreadstalker.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DreadstalkerMod.MODID)
public final class DreadstalkerMod {
    public static final String MODID = "dreadstalker";

    public DreadstalkerMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DreadstalkerConfig.COMMON_SPEC, "dreadstalker-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DreadstalkerConfig.CLIENT_SPEC, "dreadstalker-client.toml");
    }
}

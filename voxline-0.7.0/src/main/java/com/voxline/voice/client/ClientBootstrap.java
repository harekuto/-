package com.voxline.voice.client;

import com.voxline.voice.client.gui.VoiceSettingsScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

public final class ClientBootstrap {
    private ClientBootstrap(){}
    public static void init(IEventBus modBus){ClientConfig.INSTANCE.load();modBus.addListener(KeyBindings::register);ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,()->new ConfigScreenHandler.ConfigScreenFactory((mc,parent)->new VoiceSettingsScreen(parent)));MinecraftForge.EVENT_BUS.register(new ClientEvents());}
}

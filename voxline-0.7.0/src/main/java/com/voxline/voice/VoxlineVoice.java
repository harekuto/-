package com.voxline.voice;

import com.mojang.logging.LogUtils;
import com.voxline.voice.client.ClientBootstrap;
import com.voxline.voice.network.VoiceNetwork;
import com.voxline.voice.server.ServerEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(VoxlineVoice.MOD_ID)
public final class VoxlineVoice {
    public static final String MOD_ID = "voxline";
    public static final String VERSION = "0.7.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VoxlineVoice() {
        VoiceNetwork.register();
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientBootstrap.init(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }
}

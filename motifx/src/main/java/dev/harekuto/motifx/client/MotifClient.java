package dev.harekuto.motifx.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.harekuto.motifx.MotifX;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MotifX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MotifClient {
    public static final KeyMapping INSPECTOR_KEY = new KeyMapping(
        "key.motifx.inspector",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_F8,
        "key.categories.motifx"
    );

    private MotifClient() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(INSPECTOR_KEY);
    }
}

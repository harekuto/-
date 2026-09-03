package dev.harekuto.motifx.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.harekuto.motifx.MotifX;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = MotifX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final KeyMapping INSPECTOR = new KeyMapping(
            "key.motifx.inspector", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, "key.categories.motifx");

    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(INSPECTOR);
    }

    @Mod.EventBusSubscriber(modid = MotifX.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeEvents {
        private ForgeEvents() {}

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getInstance();
            while (INSPECTOR.consumeClick()) {
                if (minecraft.screen instanceof MotifInspectorScreen) {
                    minecraft.setScreen(null);
                } else if (minecraft.player != null) {
                    minecraft.setScreen(new MotifInspectorScreen());
                }
            }
        }
    }
}

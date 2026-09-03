package dev.harekuto.motifx.client;

import dev.harekuto.motifx.MotifX;
import dev.harekuto.motifx.client.screen.MotifInspectorScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MotifX.MOD_ID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (MotifClient.INSPECTOR_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof MotifInspectorScreen) minecraft.setScreen(null);
            else minecraft.setScreen(new MotifInspectorScreen());
        }
    }
}

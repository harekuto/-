package com.harekuto.dreadstalker.client;

import com.harekuto.dreadstalker.DreadstalkerMod;
import com.harekuto.dreadstalker.config.DreadstalkerConfig;
import com.harekuto.dreadstalker.entity.DreadstalkerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DreadstalkerMod.MODID, value = Dist.CLIENT)
public final class ClientHorrorEvents {
    private static float terror;
    private static float targetTerror;
    private static int scanTicker;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            terror = 0.0F;
            targetTerror = 0.0F;
            return;
        }

        if (++scanTicker >= 4) {
            scanTicker = 0;
            targetTerror = calculateNearbyTerror(minecraft);
        }
        terror = Mth.lerp(0.18F, terror, targetTerror);
        if (terror < 0.001F && targetTerror <= 0.0F) terror = 0.0F;
    }

    private static float calculateNearbyTerror(Minecraft minecraft) {
        float strongest = 0.0F;
        for (DreadstalkerEntity stalker : minecraft.level.getEntitiesOfClass(
                DreadstalkerEntity.class,
                minecraft.player.getBoundingBox().inflate(30.0D),
                entity -> entity.isAlive())) {
            double distance = minecraft.player.distanceTo(stalker);
            if (distance > 30.0D) continue;
            float proximity = Mth.clamp(1.0F - (float) distance / 28.0F, 0.0F, 1.0F);
            float state = stalker.getThreatLevel();
            float observation = stalker.isObserved() ? 0.48F : 1.0F;
            float candidate = proximity * (0.45F + state * 0.70F) * observation;
            if (stalker.getHorrorState() == DreadstalkerEntity.STATE_RAGE) candidate += proximity * 0.16F;
            strongest = Math.max(strongest, Mth.clamp(candidate, 0.0F, 1.0F));
        }
        return strongest;
    }

    @SubscribeEvent
    public static void camera(ViewportEvent.ComputeCameraAngles event) {
        if (terror < 0.035F || !DreadstalkerConfig.CLIENT.cameraEffects.get()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        double strength = DreadstalkerConfig.CLIENT.cameraEffectStrength.get();
        double time = minecraft.level.getGameTime() + event.getPartialTick();
        float intensity = terror * terror * (float) strength;
        float roll = (float) Math.sin(time * 0.47D) * intensity * 0.72F;
        float yaw = (float) Math.sin(time * 0.73D + 1.2D) * intensity * 0.34F;
        float pitch = (float) Math.cos(time * 0.61D + 0.4D) * intensity * 0.24F;
        event.setRoll(event.getRoll() + roll);
        event.setYaw(event.getYaw() + yaw);
        event.setPitch(event.getPitch() + pitch);
    }

    @SubscribeEvent
    public static void renderGui(RenderGuiEvent.Post event) {
        if (terror < 0.045F || !DreadstalkerConfig.CLIENT.terrorVignette.get()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int maxEdge = Math.max(12, Math.min(width, height) / 9);
        int layers = 5;
        int baseAlpha = Mth.clamp((int) (terror * 66.0F), 4, 66);

        for (int layer = 0; layer < layers; layer++) {
            float falloff = 1.0F - layer / (float) layers;
            int alpha = Mth.clamp((int) (baseAlpha * falloff * falloff), 0, 72);
            int red = Mth.clamp((int) (22 + terror * 34.0F), 22, 56);
            int color = (alpha << 24) | (red << 16) | 0x000304;
            int inset = layer * maxEdge / layers;
            int thickness = Math.max(2, maxEdge / layers);
            graphics.fill(inset, inset, width - inset, inset + thickness, color);
            graphics.fill(inset, height - inset - thickness, width - inset, height - inset, color);
            graphics.fill(inset, inset + thickness, inset + thickness, height - inset - thickness, color);
            graphics.fill(width - inset - thickness, inset + thickness, width - inset, height - inset - thickness, color);
        }
    }

    private ClientHorrorEvents() {}
}

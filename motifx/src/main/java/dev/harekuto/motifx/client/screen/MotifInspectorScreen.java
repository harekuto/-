package dev.harekuto.motifx.client.screen;

import dev.harekuto.motifx.MotifX;
import dev.harekuto.motifx.runtime.MotifRuntime;
import dev.harekuto.motifx.runtime.RuntimeMetrics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class MotifInspectorScreen extends Screen {
    public MotifInspectorScreen() {
        super(Component.translatable("screen.motifx.inspector"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = Math.min(420, width - 24);
        int panelHeight = Math.min(260, height - 24);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left, top, right, bottom, 0xD014171C);
        graphics.fill(left, top, right, top + 2, 0xFF6ED6FF);
        graphics.drawCenteredString(font, Component.literal("MotifX Inspector  " + MotifX.VERSION), width / 2, top + 12, 0xFFFFFFFF);

        RuntimeMetrics.Snapshot metrics = MotifRuntime.INSTANCE.metrics().snapshot();
        List<String> lines = new ArrayList<>();
        lines.add("Runtime features: " + MotifRuntime.INSTANCE.features().size());
        lines.add("Compatibility adapters: " + MotifRuntime.INSTANCE.compatibility().snapshot().size());
        lines.add("Pose contributors: " + MotifRuntime.INSTANCE.posePipeline().snapshot().size());
        lines.add("Pose evaluations: " + metrics.poseEvaluations());
        lines.add("Graph updates: " + metrics.graphUpdates());
        lines.add("Compatibility passes: " + metrics.compatibilityPasses());
        lines.add("Validations: " + metrics.validations() + "  errors: " + metrics.validationErrors());
        lines.add("");
        lines.add("F8: close inspector");
        lines.add("/motifx selftest: deterministic runtime check");
        lines.add("/motifx capabilities: list enabled core capabilities");

        int y = top + 38;
        for (String line : lines) {
            graphics.drawString(font, line, left + 14, y, 0xFFE6EDF3, false);
            y += 12;
            if (y > bottom - 14) break;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}

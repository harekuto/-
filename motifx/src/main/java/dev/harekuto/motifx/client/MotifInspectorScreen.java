package dev.harekuto.motifx.client;

import dev.harekuto.motifx.MotifX;
import dev.harekuto.motifx.internal.MotifRuntimeMetrics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MotifInspectorScreen extends Screen {
    public MotifInspectorScreen() {
        super(Component.literal("Motif Inspector"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xE0101116);
        int panelWidth = Math.min(520, width - 32);
        int left = (width - panelWidth) / 2;
        int top = Math.max(20, (height - 230) / 2);
        graphics.fill(left, top, left + panelWidth, top + 230, 0xF01B1D24);
        graphics.fill(left, top, left + panelWidth, top + 3, 0xFF7B61FF);

        MotifRuntimeMetrics.Snapshot metrics = MotifRuntimeMetrics.snapshot();
        int x = left + 18;
        int y = top + 16;
        graphics.drawString(font, Component.literal("Motif Inspector"), x, y, 0xFFF2F3F7, false);
        y += 18;
        graphics.drawString(font, Component.literal("Runtime " + MotifX.VERSION + "  •  Forge 1.20.1"), x, y, 0xFFAAAFC0, false);
        y += 26;
        graphics.drawString(font, Component.literal("Core evaluations: " + metrics.evaluations()), x, y, 0xFFE7E8ED, false);
        y += 15;
        graphics.drawString(font, Component.literal(String.format(java.util.Locale.ROOT,
                "Average evaluation: %.3f µs", metrics.averageEvaluationMicros())), x, y, 0xFFE7E8ED, false);
        y += 15;
        graphics.drawString(font, Component.literal("Graph transitions: " + metrics.graphTransitions()), x, y, 0xFFE7E8ED, false);
        y += 15;
        graphics.drawString(font, Component.literal("Asset failures: " + metrics.assetFailures()), x, y, 0xFFE7E8ED, false);
        y += 26;
        graphics.drawString(font, Component.literal("MVP modules"), x, y, 0xFFBDAFFF, false);
        y += 15;
        graphics.drawString(font, Component.literal("Skeleton • Clip • Layers • State Graph • Validator • JSON v1"), x, y, 0xFFD5D7DF, false);
        y += 24;
        graphics.drawString(font, Component.literal("F8 toggles inspector. ESC also closes it."), x, y, 0xFF8F95A8, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

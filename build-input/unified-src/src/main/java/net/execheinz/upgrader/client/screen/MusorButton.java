package net.execheinz.upgrader.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** Premium pixel-native button with restrained animation and unambiguous states. */
public final class MusorButton extends AbstractWidget {
    public enum Style { TAB, PRIMARY, SECONDARY, COMPACT }

    private final Runnable action;
    private final Style style;
    private boolean selected;
    private float hoverMix;
    private long pressedUntilNanos;

    public MusorButton(int x, int y, int width, int height, Component message, Style style, Runnable action) {
        super(x, y, width, height, message);
        this.style = style;
        this.action = action;
    }

    public MusorButton selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float target = isHovered() && active ? 1F : 0F;
        hoverMix += (target - hoverMix) * 0.28F;
        boolean pressed = System.nanoTime() < pressedUntilNanos;
        int ox = getX();
        int oy = getY() + (pressed ? 1 : 0);

        int border;
        int fill;
        if (!active) {
            border = 0xFF29222D;
            fill = 0xFF0D0A10;
        } else if (style == Style.PRIMARY) {
            border = MusorTheme.mix(MusorTheme.ACCENT_2, MusorTheme.ACCENT_GLOW, 0.18F + hoverMix * 0.56F);
            fill = MusorTheme.mix(0xFF22122C, 0xFF32173E, hoverMix * 0.80F);
        } else if (style == Style.TAB) {
            border = selected
                ? MusorTheme.mix(MusorTheme.BORDER, MusorTheme.ACCENT, 0.32F)
                : MusorTheme.mix(MusorTheme.BORDER_FAINT, MusorTheme.BORDER, hoverMix * 0.60F);
            fill = selected
                ? MusorTheme.mix(MusorTheme.PANEL, MusorTheme.ACCENT, 0.08F + hoverMix * 0.04F)
                : MusorTheme.mix(MusorTheme.BG_SOFT, MusorTheme.PANEL_HOVER, hoverMix * 0.62F);
        } else if (style == Style.COMPACT) {
            border = MusorTheme.mix(MusorTheme.BORDER_FAINT, MusorTheme.ACCENT_DARK, hoverMix * 0.75F);
            fill = MusorTheme.mix(MusorTheme.PANEL_DEEP, MusorTheme.PANEL_HOVER, hoverMix * 0.64F);
        } else {
            border = MusorTheme.mix(MusorTheme.BORDER_SOFT, MusorTheme.ACCENT_2, hoverMix * 0.64F);
            fill = MusorTheme.mix(MusorTheme.PANEL, MusorTheme.PANEL_RAISED, hoverMix * 0.75F);
        }

        MusorTheme.panel(g, ox, oy, width, height, fill, border);

        if (style == Style.TAB && selected) {
            g.fill(ox + 4, oy + height - 2, ox + width - 4, oy + height, MusorTheme.ACCENT);
        } else if (style == Style.PRIMARY && active) {
            int line = Math.max(10, (int) ((width - 14) * (0.26F + hoverMix * 0.38F)));
            g.fill(ox + 7, oy + 2, ox + 7 + line, oy + 3, MusorTheme.ACCENT_GLOW);
        } else if (active && hoverMix > 0.32F) {
            g.fill(ox + 5, oy + height - 2, ox + width - 5, oy + height - 1,
                MusorTheme.mix(MusorTheme.ACCENT_DARK, MusorTheme.ACCENT, hoverMix));
        }

        var font = Minecraft.getInstance().font;
        Component message = getMessage();
        int color = active ? MusorTheme.TEXT : MusorTheme.MUTED;
        int textWidth = Math.max(1, font.width(message));
        float preferred = style == Style.COMPACT ? 0.72F : style == Style.TAB ? 0.78F : 0.82F;
        float scale = Math.min(preferred, Math.max(0.50F, (width - 10F) / textWidth));
        float drawnWidth = textWidth * scale;
        float drawnHeight = 8F * scale;
        float tx = ox + (width - drawnWidth) * 0.5F;
        float ty = oy + (height - drawnHeight) * 0.5F - 0.2F;

        g.pose().pushPose();
        g.pose().translate(tx, ty, 0F);
        g.pose().scale(scale, scale, 1F);
        g.drawString(font, message, 0, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!active || action == null) return;
        pressedUntilNanos = System.nanoTime() + 70_000_000L;
        float pitch = style == Style.PRIMARY ? 1.08F : style == Style.TAB ? 1.02F : 0.98F;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
        action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}

package net.execheinz.upgrader.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/** Compact pixel-native button with calm hierarchy and clear selected/hover/disabled states. */
public final class MusorButton extends AbstractWidget {
    public enum Style { TAB, PRIMARY, SECONDARY, COMPACT }

    private final Runnable action;
    private final Style style;
    private boolean selected;
    private float hoverMix;

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
        hoverMix += (target - hoverMix) * 0.30F;

        int border;
        int fill;
        int accent = style == Style.PRIMARY ? MusorTheme.ACCENT_GLOW : MusorTheme.ACCENT;

        if (!active) {
            border = 0xFF2B2230;
            fill = 0xFF0F0B13;
        } else if (style == Style.PRIMARY) {
            border = MusorTheme.mix(MusorTheme.ACCENT_2, MusorTheme.ACCENT_GLOW, 0.34F + hoverMix * 0.42F);
            fill = MusorTheme.mix(0xFF281334, 0xFF371946, hoverMix);
        } else if (style == Style.TAB) {
            border = selected
                ? MusorTheme.mix(MusorTheme.ACCENT_2, MusorTheme.ACCENT_GLOW, 0.22F + hoverMix * 0.35F)
                : MusorTheme.mix(MusorTheme.BORDER_SOFT, MusorTheme.BORDER, hoverMix * 0.72F);
            fill = selected
                ? MusorTheme.mix(0xFF25122F, 0xFF32173F, hoverMix)
                : MusorTheme.mix(0xFF0E0A13, 0xFF17101E, hoverMix);
        } else if (style == Style.COMPACT) {
            border = MusorTheme.mix(MusorTheme.BORDER_SOFT, MusorTheme.ACCENT_2, hoverMix * 0.70F);
            fill = MusorTheme.mix(0xFF0D0912, 0xFF1A1022, hoverMix);
        } else {
            border = MusorTheme.mix(MusorTheme.BORDER, MusorTheme.ACCENT_2, hoverMix * 0.75F);
            fill = MusorTheme.mix(0xFF130C1A, 0xFF21132B, hoverMix);
        }

        MusorTheme.panel(g, getX(), getY(), width, height, fill, border);

        if (active && style == Style.PRIMARY) {
            int line = Math.max(8, (int) ((width - 12) * (0.32F + hoverMix * 0.18F)));
            g.fill(getX() + 6, getY() + 2, getX() + 6 + line, getY() + 3, accent);
        }
        if (style == Style.TAB && selected) {
            g.fill(getX() + 5, getY() + height - 2, getX() + width - 5, getY() + height - 1, MusorTheme.ACCENT);
        } else if (active && hoverMix > 0.25F && style != Style.TAB) {
            g.fill(getX() + 5, getY() + height - 2, getX() + width - 5, getY() + height - 1,
                MusorTheme.mix(MusorTheme.ACCENT_DARK, accent, hoverMix));
        }

        var font = Minecraft.getInstance().font;
        Component message = getMessage();
        int color = active ? MusorTheme.TEXT : MusorTheme.MUTED;
        int textWidth = Math.max(1, font.width(message));
        float scale = Math.min(0.88F, Math.max(0.54F, (width - 10F) / textWidth));
        float drawnWidth = textWidth * scale;
        float drawnHeight = 8F * scale;
        float tx = getX() + (width - drawnWidth) * 0.5F;
        float ty = getY() + (height - drawnHeight) * 0.5F - 0.25F;

        g.pose().pushPose();
        g.pose().translate(tx, ty, 0F);
        g.pose().scale(scale, scale, 1F);
        g.drawString(font, message, 0, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!active || action == null) return;
        float pitch = style == Style.PRIMARY ? 1.08F : style == Style.TAB ? 1.02F : 0.98F;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
        action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}

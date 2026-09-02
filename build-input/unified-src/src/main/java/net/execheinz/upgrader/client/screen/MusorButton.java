package net.execheinz.upgrader.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

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
        float target = isHovered() && active ? 1.0F : 0.0F;
        hoverMix += (target - hoverMix) * 0.28F;

        int border;
        int fill;
        if (!active) {
            border = 0xFF302737;
            fill = 0xFF130F17;
        } else if (style == Style.PRIMARY) {
            border = MusorTheme.mix(MusorTheme.ACCENT, MusorTheme.ACCENT_GLOW, hoverMix);
            fill = MusorTheme.mix(0xFF321842, 0xFF4B2464, hoverMix);
        } else if (style == Style.TAB) {
            border = selected
                ? MusorTheme.ACCENT
                : MusorTheme.mix(MusorTheme.BORDER_SOFT, MusorTheme.BORDER, hoverMix);
            fill = selected
                ? MusorTheme.mix(0xFF2B153B, 0xFF3A1D4F, hoverMix)
                : MusorTheme.mix(0xFF120B18, 0xFF21102D, hoverMix);
        } else if (style == Style.COMPACT) {
            border = MusorTheme.mix(MusorTheme.BORDER_SOFT, MusorTheme.ACCENT_2, hoverMix);
            fill = MusorTheme.mix(0xFF100A16, 0xFF251232, hoverMix);
        } else {
            border = MusorTheme.mix(MusorTheme.BORDER, MusorTheme.ACCENT_2, hoverMix);
            fill = MusorTheme.mix(0xFF180D21, 0xFF2B173A, hoverMix);
        }

        MusorTheme.panel(g, getX(), getY(), width, height, fill, border);
        if (style == Style.TAB && selected) {
            g.fill(getX() + 6, getY() + height - 3, getX() + width - 6, getY() + height - 1, MusorTheme.ACCENT);
        }
        if (style == Style.PRIMARY && active) {
            int glow = Math.max(2, (int) ((width - 12) * (0.18F + 0.12F * hoverMix)));
            g.fill(getX() + 6, getY() + 3, getX() + 6 + glow, getY() + 4, MusorTheme.ACCENT_GLOW);
        }

        var font = Minecraft.getInstance().font;
        Component msg = getMessage();
        int color = active ? MusorTheme.TEXT : MusorTheme.MUTED;
        int textWidth = Math.max(1, font.width(msg));
        float scale = Math.min(1.0F, Math.max(0.68F, (width - 10F) / textWidth));
        float drawnWidth = textWidth * scale;
        float drawnHeight = 8F * scale;
        float tx = getX() + (width - drawnWidth) * 0.5F;
        float ty = getY() + (height - drawnHeight) * 0.5F;

        g.pose().pushPose();
        g.pose().translate(tx, ty, 0F);
        g.pose().scale(scale, scale, 1F);
        g.drawString(font, msg, 0, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!active || action == null) return;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}

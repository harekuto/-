package net.execheinz.upgrader.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public final class MusorButton extends AbstractWidget {
    public enum Style { TAB, PRIMARY, SECONDARY, COMPACT }

    private final Runnable action;
    private final Style style;
    private boolean selected;

    public MusorButton(int x, int y, int width, int height, Component message, Style style, Runnable action) {
        super(x, y, width, height, message);
        this.style = style;
        this.action = action;
    }

    public MusorButton selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hover = isHovered();
        int border;
        int fill;
        if (!active) {
            border = 0xFF33283A;
            fill = 0xFF151018;
        } else if (style == Style.PRIMARY) {
            border = hover ? MusorTheme.brighten(MusorTheme.ACCENT, 20) : MusorTheme.ACCENT;
            fill = hover ? 0xFF4A2562 : 0xFF351A47;
        } else if (style == Style.TAB) {
            border = selected ? MusorTheme.ACCENT : (hover ? MusorTheme.BORDER : MusorTheme.BORDER_SOFT);
            fill = selected ? 0xFF3A1D4F : (hover ? 0xFF251331 : 0xFF17101E);
        } else {
            border = hover ? MusorTheme.ACCENT_2 : MusorTheme.BORDER;
            fill = hover ? 0xFF2D173D : 0xFF1B1024;
        }

        MusorTheme.panel(g, getX(), getY(), width, height, fill, border);
        if (style == Style.TAB && selected) {
            g.fill(getX() + 5, getY() + height - 3, getX() + width - 5, getY() + height - 1, MusorTheme.ACCENT);
        }

        var font = Minecraft.getInstance().font;
        Component msg = getMessage();
        int color = active ? MusorTheme.TEXT : MusorTheme.MUTED;
        int tx = getX() + Math.max(4, (width - font.width(msg)) / 2);
        int ty = getY() + Math.max(3, (height - 8) / 2);
        g.drawString(font, msg, tx, ty, color, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (active && action != null) action.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}

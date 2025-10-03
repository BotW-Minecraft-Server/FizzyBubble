package link.botwmcs.fizzy.ui.frame;

import net.minecraft.client.gui.GuiGraphics;

public class SolidColorPainter implements FramePainter {
    private final int argb;         // 例如 0xCC101010
    private final int edgeColor;    // 底边色，例 0x66000000
    private final int edgeH;        // 底边高度

    public SolidColorPainter(int argb, int edgeColor, int edgeH) {
        this.argb = argb;
        this.edgeColor = edgeColor;
        this.edgeH = Math.max(0, edgeH);
    }

    @Override
    public void paint(GuiGraphics g, int left, int top, int w, int h, boolean drawBottomEdge) {
        g.fill(left, top, left + w, top + h, argb);
        if (drawBottomEdge && edgeH > 0) {
            g.fill(left, top + h - edgeH, left + w, top + h, edgeColor);
        }
    }

}

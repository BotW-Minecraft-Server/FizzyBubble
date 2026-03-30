package link.botwmcs.fizzy.ui.background;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SoildColorBg implements BgPainter {
    private int color;

    public SoildColorBg(int color) {
        this.color = color;
    }

    @Override
    public void paint(GuiGraphicsExtractor g, FramePainter painter) {
        var area = painter.currentBackgroundArea();
        if (area.h() <= 0 || area.w() <= 0) return;
        final int x = area.x();
        final int y = area.y();
        final int w = area.w();
        final int h = area.h();

        g.fill(x, y, w, h, color);
    }
}

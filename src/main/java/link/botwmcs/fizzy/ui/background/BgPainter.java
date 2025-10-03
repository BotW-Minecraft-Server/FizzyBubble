package link.botwmcs.fizzy.ui.background;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;

public interface BgPainter {
    void paint(GuiGraphics g, FramePainter painter);
}

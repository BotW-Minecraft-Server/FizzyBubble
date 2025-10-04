package link.botwmcs.fizzy.ui.behind;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;

public interface BehindPainter {
    void paint(GuiGraphics g, FramePainter painter, float partialTick);
    BehindType type();
}

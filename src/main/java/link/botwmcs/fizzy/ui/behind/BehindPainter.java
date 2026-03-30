package link.botwmcs.fizzy.ui.behind;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface BehindPainter {
    void paint(GuiGraphicsExtractor g, FramePainter painter, float partialTick);
    BehindType type();
}

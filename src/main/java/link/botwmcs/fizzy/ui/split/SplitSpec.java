package link.botwmcs.fizzy.ui.split;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;

public interface SplitSpec {
    void paint(GuiGraphics g, SplitPainter painter, FramePainter.SlotArea area);
}

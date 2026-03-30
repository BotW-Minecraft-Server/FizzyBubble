package link.botwmcs.fizzy.ui.split;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface SplitSpec {
    void paint(GuiGraphicsExtractor g, SplitPainter painter, FramePainter.SlotArea area);
}

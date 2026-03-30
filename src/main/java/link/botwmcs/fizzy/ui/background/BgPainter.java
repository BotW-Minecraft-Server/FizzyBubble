package link.botwmcs.fizzy.ui.background;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface BgPainter {
    void paint(GuiGraphicsExtractor g, FramePainter painter);
}

package link.botwmcs.fizzy.ui.behind;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BlurBehind implements BehindPainter {
    @Override
    public void paint(GuiGraphicsExtractor g, FramePainter painter, float partialTick) {
        g.blurBeforeThisStratum();
    }

    @Override
    public BehindType type() {
        return BehindType.BLUR;
    }
}

package link.botwmcs.fizzy.ui.behind;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class VanillaBehind implements BehindPainter {
    private static final int TOP_COLOR = 0xC0101010;
    private static final int BOTTOM_COLOR = 0xD0101010;

    @Override
    public void paint(GuiGraphicsExtractor g, FramePainter painter, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        g.blurBeforeThisStratum();
        g.fillGradient(0, 0, screenW, screenH, TOP_COLOR, BOTTOM_COLOR);
    }

    @Override
    public BehindType type() {
        return BehindType.BLUR;
    }
}

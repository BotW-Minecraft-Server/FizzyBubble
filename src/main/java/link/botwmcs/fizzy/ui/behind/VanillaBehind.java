package link.botwmcs.fizzy.ui.behind;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaBehind implements BehindPainter {
    private static final int TOP_COLOR = 0xC0101010;
    private static final int BOTTOM_COLOR = 0xD0101010;

    @Override
    public void paint(GuiGraphics g, FramePainter painter, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        g.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        mc.gameRenderer.processBlurEffect(partialTick);
        mc.getMainRenderTarget().bindWrite(false);
        g.fillGradient(0, 0, screenW, screenH, TOP_COLOR, BOTTOM_COLOR);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public BehindType type() {
        return BehindType.BLUR;
    }
}

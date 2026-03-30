package link.botwmcs.fizzy.ui.behind;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BlurBehind implements BehindPainter {
    @Override
    public void paint(GuiGraphicsExtractor g, FramePainter painter, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        g.flush();
        RenderSystem.disableDepthTest();
        mc.gameRenderer.processBlurEffect(partialTick);
        mc.getMainRenderTarget().bindWrite(false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public BehindType type() {
        return BehindType.BLUR;
    }
}

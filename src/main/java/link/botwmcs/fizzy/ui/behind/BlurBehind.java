package link.botwmcs.fizzy.ui.behind;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class BlurBehind implements BehindPainter {
    @Override
    public void paint(GuiGraphics g, FramePainter painter, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        RenderSystem.disableDepthTest();
        mc.gameRenderer.processBlurEffect(partialTick);
        mc.getMainRenderTarget().bindWrite(false);
    }

    @Override
    public BehindType type() {
        return BehindType.BLUR;
    }
}

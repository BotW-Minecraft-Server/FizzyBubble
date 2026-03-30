package link.botwmcs.fizzy.ui.behind;

import com.mojang.blaze3d.systems.RenderSystem;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SoildColorBehind implements BehindPainter {
    private int color; // ARGB

    public SoildColorBehind(int color) {
        this.color = color;
    }

    @Override
    public void paint(GuiGraphicsExtractor g, FramePainter painter, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        g.fill(0, 0, sw, sh, color);

    }

    @Override
    public BehindType type() {
        return BehindType.SOILD_COLOR;
    }

}

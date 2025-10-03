package link.botwmcs.fizzy.ui.frame;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TextureStretchPainter implements FramePainter {
    private final ResourceLocation tex;

    public TextureStretchPainter(ResourceLocation tex) { this.tex = tex; }

    @Override
    public void paint(GuiGraphics g, int left, int top, int w, int h, boolean drawBottomEdge) {
        // 将整张纹理拉伸到目标区域（简单粗暴，先满足“把 BG 渲染出来”）
        g.blit(tex, left, top, 0, 0, w, h, w, h);
        // 底边通常不需要额外绘制，有需要可加半透明条或阴影
    }

}

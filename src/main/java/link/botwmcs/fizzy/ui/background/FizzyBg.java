package link.botwmcs.fizzy.ui.background;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class FizzyBg implements BgPainter {
    private final BgType type;

    private static final Identifier TEX_STONE =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background1.png");
    private static final Identifier TEX_BARRIER =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background2.png");
    private static final Identifier TEX_BARRIER_BLUE =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background3.png");
    private static final Identifier TEX_PURE_GRAY =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background4.png");
    private static final Identifier TEX_BOTW =
            Identifier.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background5.png");

    public FizzyBg(BgType type) {
        this.type = type;
    }

    @Override
    public void paint(GuiGraphicsExtractor g, FramePainter painter) {
        var area = painter.currentBackgroundArea();
        if (area.w() <= 0 || area.h() <= 0) {
            return;
        }

        int x = area.x();
        int y = area.y();
        int w = area.w();
        int h = area.h();

        switch (type) {
            case PURE_GRAY -> tile(g, TEX_PURE_GRAY, x, y, w, h);
            case STONE -> tile(g, TEX_STONE, x, y, w, h);
            case BARRIER -> tile(g, TEX_BARRIER, x, y, w, h);
            case BARRIER_BLUE -> tile(g, TEX_BARRIER_BLUE, x, y, w, h);
            case BOTW -> tile(g, TEX_BOTW, x, y, w, h);
        }
    }

    private static void tile(GuiGraphicsExtractor g, Identifier texture, int x, int y, int w, int h) {
        FizzyGuiUtils.TextureSize size = FizzyGuiUtils.textureSize(texture);
        int tileW = Math.max(1, size.w());
        int tileH = Math.max(1, size.h());

        int yy = y;
        int remH = h;
        while (remH > 0) {
            int dh = Math.min(tileH, remH);
            int xx = x;
            int remW = w;
            while (remW > 0) {
                int dw = Math.min(tileW, remW);
                g.blit(RenderPipelines.GUI_TEXTURED, texture, xx, yy, 0.0f, 0.0f, dw, dh, dw, dh, tileW, tileH);
                xx += dw;
                remW -= dw;
            }
            yy += dh;
            remH -= dh;
        }
    }
}

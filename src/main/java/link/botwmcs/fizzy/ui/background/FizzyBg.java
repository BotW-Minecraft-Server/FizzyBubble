package link.botwmcs.fizzy.ui.background;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class FizzyBg implements BgPainter {
    private final BgType type;

    private static final ResourceLocation TEX_STONE =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background1.png");
    private static final ResourceLocation TEX_BARRIER =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background2.png");
    private static final ResourceLocation TEX_BARRIER_BLUE =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background3.png");
    private static final ResourceLocation TEX_PURE_GRAY =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background4.png");
    private static final ResourceLocation TEX_BOTW =
            ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background5.png");

    public FizzyBg(BgType type) {
        this.type = type;
    }

    @Override
    public void paint(GuiGraphics g, FramePainter painter) {
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

    private static void tile(GuiGraphics g, ResourceLocation texture, int x, int y, int w, int h) {
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
                g.blit(texture, xx, yy, 0, 0, dw, dh, tileW, tileH);
                xx += dw;
                remW -= dw;
            }
            yy += dh;
            remH -= dh;
        }
    }
}

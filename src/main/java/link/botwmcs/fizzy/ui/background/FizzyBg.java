package link.botwmcs.fizzy.ui.background;

import com.mojang.blaze3d.platform.NativeImage;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FizzyBg implements BgPainter {
    private final BgType type;

    private static final ResourceLocation TEX_STONE = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background1.png");
    private static final ResourceLocation TEX_BARRIER = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background2.png");
    private static final ResourceLocation TEX_BARRIER_BLUE = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background3.png");
    private static final ResourceLocation TEX_PURE_GRAY = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background4.png");
    private static final ResourceLocation TEX_BOTW = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background5.png");

    public FizzyBg(BgType type) {
        this.type = type;
    }

    @Override
    public void paint(GuiGraphics g, FramePainter painter) {
        var area = painter.currentSlotArea();
        if (area.h() <= 0 || area.w() <= 0) return;
        final int x = area.x();
        final int y = area.y();
        final int w = area.w();
        final int h = area.h();

        switch (type) {
            case PURE_GRAY -> tile(g, TEX_PURE_GRAY, x, y, w, h);
            case STONE -> tile(g, TEX_STONE, x, y, w, h);
            case BARRIER -> tile(g, TEX_BARRIER, x, y, w, h);
            case BARRIER_BLUE -> tile(g, TEX_BARRIER_BLUE, x, y, w, h);
            case BOTW -> tile(g, TEX_BOTW, x, y, w, h);
        }
    }

    private record TextureSize(int w, int h) {
        static final TextureSize FALLBACK = new TextureSize(16, 16);
    }

    private static final Map<ResourceLocation, TextureSize> SIZE_CACHE = new ConcurrentHashMap<>();


    /** 在 (x,y,w,h) 内用 tileW×tileH 的纹理块进行平铺。 */
    private static void tile(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h) {
        TextureSize size = SIZE_CACHE.computeIfAbsent(tex, FizzyBg::resolveTextureSize);
        int yy = y, remH = h;
        int tileW = Math.max(1, size.w());
        int tileH = Math.max(1, size.h());
        while (remH > 0) {
            int dh = Math.min(tileH, remH);
            int xx = x, remW = w;
            while (remW > 0) {
                int dw = Math.min(tileW, remW);
                // 目标绘制大小是 (dw, dh)；源声明大小是 (tileW, tileH)
                g.blit(tex, xx, yy, 0, 0, dw, dh, tileW, tileH);
                xx += dw; remW -= dw;
            }
            yy += dh; remH -= dh;
        }
    }

    private static TextureSize resolveTextureSize(ResourceLocation tex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return TextureSize.FALLBACK;
        }
        try {
            var resourceOpt = mc.getResourceManager().getResource(tex);
            if (resourceOpt.isEmpty()) {
                return TextureSize.FALLBACK;
            }

            Resource resource = resourceOpt.get();
            try (InputStream stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width <= 0 || height <= 0) {
                    return TextureSize.FALLBACK;
                }
                return new TextureSize(width, height);
            }
        } catch (IOException e) {
            return TextureSize.FALLBACK;
        }
    }
}

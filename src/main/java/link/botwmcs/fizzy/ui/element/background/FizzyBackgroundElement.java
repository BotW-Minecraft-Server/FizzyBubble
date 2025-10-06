package link.botwmcs.fizzy.ui.element.background;

import com.mojang.blaze3d.platform.NativeImage;
import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.ui.background.BgType;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FizzyBackgroundElement implements ElementPainter {
    private final BgType type;

    private static final ResourceLocation TEX_STONE = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background1.png");
    private static final ResourceLocation TEX_BARRIER = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background2.png");
    private static final ResourceLocation TEX_BARRIER_BLUE = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background3.png");
    private static final ResourceLocation TEX_PURE_GRAY = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background4.png");
    private static final ResourceLocation TEX_BOTW = ResourceLocation.fromNamespaceAndPath(Fizzy.MODID, "textures/gui/ui/background/background5.png");

    private static final Map<BgType, ResourceLocation> TEXTURES = new EnumMap<>(BgType.class);
    static {
        TEXTURES.put(BgType.STONE, TEX_STONE);
        TEXTURES.put(BgType.BARRIER, TEX_BARRIER);
        TEXTURES.put(BgType.BARRIER_BLUE, TEX_BARRIER_BLUE);
        TEXTURES.put(BgType.PURE_GRAY, TEX_PURE_GRAY);
        TEXTURES.put(BgType.BOTW, TEX_BOTW);
    }

    public FizzyBackgroundElement(BgType type) {
        this.type = type;
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }
        ResourceLocation tex = TEXTURES.get(type);
        if (tex == null) {
            return;
        }

        tile(g, tex, leftPx, topPx, widthPx, heightPx);
    }

    private record TextureSize(int w, int h) {
        static final TextureSize FALLBACK = new TextureSize(16, 16);
    }

    private static final Map<ResourceLocation, TextureSize> SIZE_CACHE = new ConcurrentHashMap<>();

    private static void tile(GuiGraphics g, ResourceLocation tex, int x, int y, int w, int h) {
        TextureSize size = SIZE_CACHE.computeIfAbsent(tex, FizzyBackgroundElement::resolveTextureSize);
        int yy = y, remH = h;
        int tileW = Math.max(1, size.w());
        int tileH = Math.max(1, size.h());
        while (remH > 0) {
            int dh = Math.min(tileH, remH);
            int xx = x, remW = w;
            while (remW > 0) {
                int dw = Math.min(tileW, remW);
                g.blit(tex, xx, yy, 0, 0, dw, dh, tileW, tileH);
                xx += dw;
                remW -= dw;
            }
            yy += dh;
            remH -= dh;
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

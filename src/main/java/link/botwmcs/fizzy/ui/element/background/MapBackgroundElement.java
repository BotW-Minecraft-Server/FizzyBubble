package link.botwmcs.fizzy.ui.element.background;

import com.mojang.blaze3d.platform.NativeImage;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MapBackgroundElement implements ElementPainter {
    private static final ResourceLocation MAP_BG = ResourceLocation.withDefaultNamespace("textures/map/map_background.png");
    private static final int BORDER_PX = 3;

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }

        TextureSize size = SIZE_CACHE.computeIfAbsent(MAP_BG, MapBackgroundElement::resolveTextureSize);
        int texW = Math.max(1, size.w());
        int texH = Math.max(1, size.h());

        int inset = 1;
        int drawX = leftPx + inset;
        int drawY = topPx + inset;
        int drawW = widthPx - inset * 2;
        int drawH = heightPx - inset * 2;
        if (drawW <= 0 || drawH <= 0) {
            return;
        }

        drawNineSlice(g, MAP_BG, drawX, drawY, drawW, drawH, texW, texH, BORDER_PX);
    }

    @Override
    public ElementType type() {
        return ElementType.IMAGE;
    }

    private static void drawNineSlice(GuiGraphics g, ResourceLocation tex,
                                      int x, int y, int w, int h,
                                      int texW, int texH, int border) {
        if (w <= 0 || h <= 0) {
            return;
        }

        int srcBorderX = Math.min(border, texW);
        int srcBorderY = Math.min(border, texH);
        int srcRightU = Math.max(0, texW - srcBorderX);
        int srcBottomV = Math.max(0, texH - srcBorderY);

        int leftW = Math.min(border, w);
        int rightW = Math.min(border, w - leftW);
        int centerW = Math.max(0, w - leftW - rightW);

        int topH = Math.min(border, h);
        int bottomH = Math.min(border, h - topH);
        int centerH = Math.max(0, h - topH - bottomH);

        int centerX = x + leftW;
        int centerY = y + topH;
        int rightX = x + leftW + centerW;
        int bottomY = y + topH + centerH;

        if (leftW > 0 && topH > 0) {
            g.blit(tex, x, y, 0, 0, leftW, topH, texW, texH);
        }
        if (centerW > 0 && topH > 0) {
            g.blit(tex, centerX, y, srcBorderX, 0, centerW, topH, texW, texH);
        }
        if (rightW > 0 && topH > 0) {
            g.blit(tex, rightX, y, srcRightU, 0, rightW, topH, texW, texH);
        }

        if (leftW > 0 && centerH > 0) {
            g.blit(tex, x, centerY, 0, srcBorderY, leftW, centerH, texW, texH);
        }
        if (centerW > 0 && centerH > 0) {
            g.blit(tex, centerX, centerY, srcBorderX, srcBorderY, centerW, centerH, texW, texH);
        }
        if (rightW > 0 && centerH > 0) {
            g.blit(tex, rightX, centerY, srcRightU, srcBorderY, rightW, centerH, texW, texH);
        }

        if (leftW > 0 && bottomH > 0) {
            g.blit(tex, x, bottomY, 0, srcBottomV, leftW, bottomH, texW, texH);
        }
        if (centerW > 0 && bottomH > 0) {
            g.blit(tex, centerX, bottomY, srcBorderX, srcBottomV, centerW, bottomH, texW, texH);
        }
        if (rightW > 0 && bottomH > 0) {
            g.blit(tex, rightX, bottomY, srcRightU, srcBottomV, rightW, bottomH, texW, texH);
        }
    }

    private record TextureSize(int w, int h) {
        static final TextureSize FALLBACK = new TextureSize(16, 16);
    }

    private static final Map<ResourceLocation, TextureSize> SIZE_CACHE = new ConcurrentHashMap<>();

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

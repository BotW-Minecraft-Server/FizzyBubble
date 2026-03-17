package link.botwmcs.fizzy.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared client-side GUI helpers used by Fizzy elements and widgets.
 */
public final class FizzyGuiUtils {
    private static final TextureSize FALLBACK_TEXTURE_SIZE = new TextureSize(16, 16);
    private static final Map<ResourceLocation, TextureSize> TEXTURE_SIZE_CACHE = new ConcurrentHashMap<>();
    private static ResourceLocation blankTextureLocation;


    private FizzyGuiUtils() {
    }

    /**
     * Returns a 1x1 white texture registered in the texture manager.
     */
    public static ResourceLocation getBlankTexture() {
        if (blankTextureLocation == null) {
            NativeImage img = new NativeImage(1, 1, false);
            img.setPixelRGBA(0, 0, 0xFFFFFFFF);
            blankTextureLocation = Minecraft.getInstance().getTextureManager().register("blank", new DynamicTexture(img));
        }

        return blankTextureLocation;
    }

    /**
     * Chooses whether a dark foreground should be used on top of the given RGB color.
     *
     * @return true when white text is recommended, false when black text is recommended
     */
    public static boolean useWhiteOrBlackForeColor(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        double luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255;
        return luminance < 0.5;
    }

    /**
     * Applies position and size to a widget if the widget is non-null.
     */
    public static void syncWidgetBounds(AbstractWidget widget, int x, int y, int width, int height) {
        if (widget == null) {
            return;
        }
        widget.setX(x);
        widget.setY(y);
        widget.setWidth(width);
        widget.setHeight(height);
    }

    /**
     * Returns cached texture dimensions for the given resource.
     */
    public static TextureSize textureSize(ResourceLocation texture) {
        return TEXTURE_SIZE_CACHE.computeIfAbsent(texture, FizzyGuiUtils::resolveTextureSize);
    }

    /**
     * Draws a texture into the target rectangle using full opacity.
     */
    public static void drawTextureFit(GuiGraphics g,
                                      ResourceLocation texture,
                                      int x,
                                      int y,
                                      int width,
                                      int height,
                                      boolean stretchToFit,
                                      boolean allowUpscale) {
        drawTextureFit(g, texture, x, y, width, height, stretchToFit, allowUpscale, 1.0f);
    }

    /**
     * Draws a texture either stretched or aspect-fitted and centered in the target rectangle.
     */
    public static void drawTextureFit(GuiGraphics g,
                                      ResourceLocation texture,
                                      int x,
                                      int y,
                                      int width,
                                      int height,
                                      boolean stretchToFit,
                                      boolean allowUpscale,
                                      float alpha) {
        if (width <= 0 || height <= 0) {
            return;
        }
        float safeAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        if (safeAlpha <= 0.0f) {
            return;
        }

        TextureSize size = textureSize(texture);
        int texW = Math.max(1, size.w());
        int texH = Math.max(1, size.h());

        int drawX = x;
        int drawY = y;
        int drawW = width;
        int drawH = height;

        if (!stretchToFit) {
            float scale = Math.min(width / (float) texW, height / (float) texH);
            if (!allowUpscale) {
                scale = Math.min(scale, 1.0f);
            }
            if (scale <= 0.0f) {
                return;
            }
            drawW = Math.max(1, Math.round(texW * scale));
            drawH = Math.max(1, Math.round(texH * scale));
            drawX = x + (width - drawW) / 2;
            drawY = y + (height - drawH) / 2;
        }

        g.setColor(1.0f, 1.0f, 1.0f, safeAlpha);
        try {
            g.blit(texture, drawX, drawY, 0, 0, drawW, drawH, texW, texH);
        } finally {
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    /**
     * Draws a nine-slice with identical border thickness on all sides.
     */
    public static void drawNineSlice(GuiGraphics g,
                                     ResourceLocation texture,
                                     int x,
                                     int y,
                                     int width,
                                     int height,
                                     int texW,
                                     int texH,
                                     int border) {
        drawNineSlice(g, texture, x, y, width, height, texW, texH, border, border, border, border);
    }

    /**
     * Draws a nine-slice with independent horizontal and vertical border thickness.
     */
    public static void drawNineSlice(GuiGraphics g,
                                     ResourceLocation texture,
                                     int x,
                                     int y,
                                     int width,
                                     int height,
                                     int texW,
                                     int texH,
                                     int borderX,
                                     int borderY) {
        drawNineSlice(g, texture, x, y, width, height, texW, texH, borderX, borderX, borderY, borderY);
    }

    /**
     * Draws a fully configurable nine-slice using explicit borders for each side.
     */
    public static void drawNineSlice(GuiGraphics g,
                                     ResourceLocation texture,
                                     int x,
                                     int y,
                                     int width,
                                     int height,
                                     int texW,
                                     int texH,
                                     int leftBorder,
                                     int rightBorder,
                                     int topBorder,
                                     int bottomBorder) {
        if (width <= 0 || height <= 0 || texW <= 0 || texH <= 0) {
            return;
        }

        int[] destX = fitBorders(width, leftBorder, rightBorder);
        int destLeft = destX[0];
        int destRight = destX[1];
        int destCenterW = width - destLeft - destRight;

        int[] destY = fitBorders(height, topBorder, bottomBorder);
        int destTop = destY[0];
        int destBottom = destY[1];
        int destCenterH = height - destTop - destBottom;

        int[] srcX = fitBorders(texW, leftBorder, rightBorder);
        int srcLeft = srcX[0];
        int srcRight = srcX[1];
        int srcCenterW = texW - srcLeft - srcRight;

        int[] srcY = fitBorders(texH, topBorder, bottomBorder);
        int srcTop = srcY[0];
        int srcBottom = srcY[1];
        int srcCenterH = texH - srcTop - srcBottom;

        if ((destCenterW > 0 && srcCenterW <= 0) || (destCenterH > 0 && srcCenterH <= 0)) {
            g.blit(texture, x, y, width, height, 0, 0, texW, texH, texW, texH);
            return;
        }

        int centerX = x + destLeft;
        int rightX = x + width - destRight;
        int centerY = y + destTop;
        int bottomY = y + height - destBottom;

        int srcRightU = texW - srcRight;
        int srcBottomV = texH - srcBottom;

        if (destLeft > 0 && destTop > 0 && srcLeft > 0 && srcTop > 0) {
            blitScaledRegion(g, texture, x, y, destLeft, destTop, 0, 0, srcLeft, srcTop, texW, texH);
        }
        if (destCenterW > 0 && destTop > 0 && srcCenterW > 0 && srcTop > 0) {
            blitScaledRegion(g, texture, centerX, y, destCenterW, destTop, srcLeft, 0, srcCenterW, srcTop, texW, texH);
        }
        if (destRight > 0 && destTop > 0 && srcRight > 0 && srcTop > 0) {
            blitScaledRegion(g, texture, rightX, y, destRight, destTop, srcRightU, 0, srcRight, srcTop, texW, texH);
        }

        if (destLeft > 0 && destCenterH > 0 && srcLeft > 0 && srcCenterH > 0) {
            blitScaledRegion(g, texture, x, centerY, destLeft, destCenterH, 0, srcTop, srcLeft, srcCenterH, texW, texH);
        }
        if (destCenterW > 0 && destCenterH > 0 && srcCenterW > 0 && srcCenterH > 0) {
            blitScaledRegion(g, texture, centerX, centerY, destCenterW, destCenterH, srcLeft, srcTop, srcCenterW, srcCenterH, texW, texH);
        }
        if (destRight > 0 && destCenterH > 0 && srcRight > 0 && srcCenterH > 0) {
            blitScaledRegion(g, texture, rightX, centerY, destRight, destCenterH, srcRightU, srcTop, srcRight, srcCenterH, texW, texH);
        }

        if (destLeft > 0 && destBottom > 0 && srcLeft > 0 && srcBottom > 0) {
            blitScaledRegion(g, texture, x, bottomY, destLeft, destBottom, 0, srcBottomV, srcLeft, srcBottom, texW, texH);
        }
        if (destCenterW > 0 && destBottom > 0 && srcCenterW > 0 && srcBottom > 0) {
            blitScaledRegion(g, texture, centerX, bottomY, destCenterW, destBottom, srcLeft, srcBottomV, srcCenterW, srcBottom, texW, texH);
        }
        if (destRight > 0 && destBottom > 0 && srcRight > 0 && srcBottom > 0) {
            blitScaledRegion(g, texture, rightX, bottomY, destRight, destBottom, srcRightU, srcBottomV, srcRight, srcBottom, texW, texH);
        }
    }

    /**
     * Draws a horizontally capped bar (left+right caps, stretched center).
     */
    public static void drawHorizontalCapNineSlice(GuiGraphics g,
                                                  ResourceLocation texture,
                                                  int x,
                                                  int y,
                                                  int width,
                                                  int height,
                                                  int texW,
                                                  int texH,
                                                  int capWidth) {
        if (width <= 0 || height <= 0 || texW <= 0 || texH <= 0) {
            return;
        }
        int safeCap = Math.max(0, Math.min(capWidth, texW / 2));
        if (safeCap <= 0 || texW - safeCap * 2 <= 0) {
            g.blit(texture, x, y, width, height, 0, 0, texW, texH, texW, texH);
            return;
        }
        drawNineSlice(g, texture, x, y, width, height, texW, texH, safeCap, safeCap, 0, 0);
    }

    /**
     * Draws a horizontally capped bar and clips it by filled width.
     */
    public static void drawScissoredHorizontalCapProgress(GuiGraphics g,
                                                          ResourceLocation texture,
                                                          int x,
                                                          int y,
                                                          int width,
                                                          int height,
                                                          int filledWidth,
                                                          int texW,
                                                          int texH,
                                                          int capWidth) {
        if (filledWidth <= 0 || width <= 0 || height <= 0) {
            return;
        }
        int clampedFill = Math.min(width, filledWidth);
        if (clampedFill <= 0) {
            return;
        }
        g.enableScissor(x, y, x + clampedFill, y + height);
        try {
            drawHorizontalCapNineSlice(g, texture, x, y, width, height, texW, texH, capWidth);
        } finally {
            g.disableScissor();
        }
    }

    /**
     * Combines an RGB color with a normalized alpha value.
     */
    public static int withAlpha(int rgb, float alpha) {
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f) & 0xFF;
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    /**
     * Draws centered text using the default Minecraft glyph height (8px).
     */
    public static void drawCenteredLabel(GuiGraphics g,
                                         Font font,
                                         Component text,
                                         int x,
                                         int y,
                                         int width,
                                         int height,
                                         int color,
                                         boolean shadow,
                                         int yOffset) {
        drawCenteredLabel(g, font, text, x, y, width, height, color, shadow, 8, yOffset);
    }

    /**
     * Draws centered text with configurable glyph height and Y offset.
     */
    public static void drawCenteredLabel(GuiGraphics g,
                                         Font font,
                                         Component text,
                                         int x,
                                         int y,
                                         int width,
                                         int height,
                                         int color,
                                         boolean shadow,
                                         int glyphHeight,
                                         int yOffset) {
        int textWidth = font.width(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - glyphHeight) / 2 + yOffset;
        g.drawString(font, text, textX, textY, color, shadow);
    }

    /**
     * Splits a component into wrapped lines and converts each line back to a plain component.
     */
    public static List<Component> splitLine(Font font, Component text, int wrapWidth) {
        if (wrapWidth <= 0) {
            return List.of();
        }
        List<FormattedCharSequence> parts = font.split(text, wrapWidth);
        List<Component> out = new ArrayList<>(parts.size());
        for (FormattedCharSequence part : parts) {
            out.add(Component.literal(toPlainString(part)));
        }
        return out;
    }

    /**
     * Converts a formatted character sequence to plain UTF-16 text.
     */
    public static String toPlainString(FormattedCharSequence sequence) {
        StringBuilder sb = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    /**
     * Disables the given widgets while recording their previous active state.
     */
    public static void disableWidgets(Iterable<? extends AbstractWidget> widgets, Map<AbstractWidget, Boolean> storedActive) {
        if (widgets == null || storedActive == null) {
            return;
        }
        for (AbstractWidget widget : widgets) {
            if (widget == null) {
                continue;
            }
            storedActive.putIfAbsent(widget, widget.active);
            widget.active = false;
        }
    }

    /**
     * Restores widget active states previously saved by {@link #disableWidgets(Iterable, Map)}.
     */
    public static void restoreWidgetStates(Map<AbstractWidget, Boolean> storedActive) {
        if (storedActive == null || storedActive.isEmpty()) {
            return;
        }
        for (Map.Entry<AbstractWidget, Boolean> entry : storedActive.entrySet()) {
            entry.getKey().active = Boolean.TRUE.equals(entry.getValue());
        }
        storedActive.clear();
    }

    /**
     * Begins a stencil region by writing a rectangular mask and enabling stencil test.
     *
     * @see <a href="http://github.com/Creators-of-Create/Create/blob/mc1.18/dev/src/main/java/com/simibubi/create/content/trains/schedule/ScheduleScreen.java">...</a>
     */
    public static void startStencil(GuiGraphics g, float x, float y, float w, float h) {
        RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(~0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);

        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(w, h, 1);
        g.fillGradient(0, 0, -100, 1, 1, 0xff000000, 0xff000000);
        g.pose().popPose();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    /**
     * Ends stencil rendering and disables stencil test.
     */
    public static void endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    /**
     * Immutable texture size (width, height).
     */
    public record TextureSize(int w, int h) {
    }

    /**
     * Fits two border values into a total span while preserving their ratio as much as possible.
     */
    private static int[] fitBorders(int total, int first, int second) {
        int safeFirst = Math.max(0, first);
        int safeSecond = Math.max(0, second);
        if (total <= 0 || (safeFirst == 0 && safeSecond == 0)) {
            return new int[] {0, 0};
        }
        int sum = safeFirst + safeSecond;
        if (sum == 0) {
            return new int[] {0, 0};
        }
        if (total >= sum) {
            return new int[] {safeFirst, safeSecond};
        }
        if (total == 1) {
            return safeFirst >= safeSecond ? new int[] {1, 0} : new int[] {0, 1};
        }

        float scale = total / (float) sum;
        int firstScaled = Math.round(safeFirst * scale);
        int secondScaled = total - firstScaled;

        if (safeFirst > 0 && firstScaled == 0) {
            firstScaled = 1;
            secondScaled = total - 1;
        } else if (safeSecond > 0 && secondScaled == 0) {
            secondScaled = 1;
            firstScaled = total - 1;
        }
        return new int[] {Math.max(0, firstScaled), Math.max(0, secondScaled)};
    }

    private static void blitScaledRegion(GuiGraphics g,
                                         ResourceLocation texture,
                                         int x,
                                         int y,
                                         int width,
                                         int height,
                                         int u,
                                         int v,
                                         int uWidth,
                                         int vHeight,
                                         int texW,
                                         int texH) {
        if (width <= 0 || height <= 0 || uWidth <= 0 || vHeight <= 0) {
            return;
        }
        g.blit(texture, x, y, width, height, u, v, uWidth, vHeight, texW, texH);
    }

    /**
     * Reads texture dimensions from resources, falling back to 16x16 when unavailable.
     */
    private static TextureSize resolveTextureSize(ResourceLocation texture) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return FALLBACK_TEXTURE_SIZE;
        }
        try {
            var resourceOpt = mc.getResourceManager().getResource(texture);
            if (resourceOpt.isEmpty()) {
                return FALLBACK_TEXTURE_SIZE;
            }
            Resource resource = resourceOpt.get();
            try (InputStream stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width <= 0 || height <= 0) {
                    return FALLBACK_TEXTURE_SIZE;
                }
                return new TextureSize(width, height);
            }
        } catch (IOException e) {
            return FALLBACK_TEXTURE_SIZE;
        }
    }

}

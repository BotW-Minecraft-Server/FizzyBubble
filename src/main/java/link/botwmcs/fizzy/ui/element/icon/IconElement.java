package link.botwmcs.fizzy.ui.element.icon;

import com.mojang.blaze3d.platform.NativeImage;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class IconElement implements AnimatableElement {
    private final ResourceLocation texture;
    private final boolean stretchToFit;
    private final boolean allowUpscale;

    public IconElement(ResourceLocation texture) {
        this(texture, false, false);
    }

    public IconElement(ResourceLocation texture, boolean stretchToFit) {
        this(texture, stretchToFit, false);
    }

    public IconElement(ResourceLocation texture, boolean stretchToFit, boolean allowUpscale) {
        this.texture = Objects.requireNonNull(texture, "texture");
        this.stretchToFit = stretchToFit;
        this.allowUpscale = allowUpscale;
    }

    private IconElement(Builder builder) {
        this.texture = builder.texture;
        this.stretchToFit = builder.stretchToFit;
        this.allowUpscale = builder.allowUpscale;
    }

    public static Builder builder(ResourceLocation texture) {
        return new Builder(texture);
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (widthPx <= 0 || heightPx <= 0) {
            return;
        }

        TextureSize size = SIZE_CACHE.computeIfAbsent(texture, IconElement::resolveTextureSize);
        int texW = Math.max(1, size.w());
        int texH = Math.max(1, size.h());

        if (stretchToFit) {
            g.blit(texture, leftPx, topPx, 0, 0, widthPx, heightPx, texW, texH);
            return;
        }

        float scale = Math.min(widthPx / (float) texW, heightPx / (float) texH);
        if (!allowUpscale) {
            scale = Math.min(scale, 1f);
        }
        if (scale <= 0f) {
            return;
        }

        int drawW = Math.max(1, Math.round(texW * scale));
        int drawH = Math.max(1, Math.round(texH * scale));
        int drawX = leftPx + (widthPx - drawW) / 2;
        int drawY = topPx + (heightPx - drawH) / 2;

        g.blit(texture, drawX, drawY, 0, 0, drawW, drawH, texW, texH);
    }

    @Override
    public ElementType type() {
        return ElementType.ICON;
    }

    public static final class Builder {
        private final ResourceLocation texture;
        private boolean stretchToFit;
        private boolean allowUpscale;

        private Builder(ResourceLocation texture) {
            this.texture = Objects.requireNonNull(texture, "texture");
        }

        public Builder stretchToFit(boolean stretch) {
            this.stretchToFit = stretch;
            return this;
        }

        public Builder stretchToFit() {
            return stretchToFit(true);
        }

        public Builder allowUpscale(boolean allowUpscale) {
            this.allowUpscale = allowUpscale;
            return this;
        }

        public Builder allowUpscale() {
            return allowUpscale(true);
        }

        public IconElement build() {
            return new IconElement(this);
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

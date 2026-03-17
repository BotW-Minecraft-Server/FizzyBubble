package link.botwmcs.fizzy.ui.element.icon;

import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

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
        FizzyGuiUtils.drawTextureFit(g, texture, leftPx, topPx, widthPx, heightPx, stretchToFit, allowUpscale);
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
}

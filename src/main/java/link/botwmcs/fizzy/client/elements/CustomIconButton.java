package link.botwmcs.fizzy.client.elements;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CustomIconButton extends CustomIconAbstractButton {
    public static final int DEFAULT_WIDTH = 16;
    public static final int DEFAULT_HEIGHT = 16;

    private final OnPress onPress;

    public static Builder builder(Component message, OnPress onPress, ResourceLocation texture) {
        return new Builder(message, onPress, texture);
    }

    public CustomIconButton(int x, int y, int width, int height, Component message,
                            ResourceLocation texture, boolean stretchToFit, boolean allowUpscale,
                            OnPress onPress) {
        super(x, y, width, height, message, texture, stretchToFit, allowUpscale);
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(CustomIconButton button);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private final ResourceLocation texture;
        private int x;
        private int y;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private boolean stretchToFit;
        private boolean allowUpscale;
        private @Nullable Tooltip tooltip;

        private Builder(Component message, OnPress onPress, ResourceLocation texture) {
            this.message = message;
            this.onPress = onPress;
            this.texture = texture;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y, int width, int height) {
            return this.pos(x, y).size(width, height);
        }

        public Builder stretchToFit(boolean stretchToFit) {
            this.stretchToFit = stretchToFit;
            return this;
        }

        public Builder allowUpscale(boolean allowUpscale) {
            this.allowUpscale = allowUpscale;
            return this;
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CustomIconButton build() {
            CustomIconButton button = new CustomIconButton(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    this.message,
                    this.texture,
                    this.stretchToFit,
                    this.allowUpscale,
                    this.onPress
            );
            button.setTooltip(this.tooltip);
            return button;
        }
    }
}

package link.botwmcs.fizzy.client.elements;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class VanillaLikeButton extends VanillaLikeAbstractButton {
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;

    protected static final CreateNarration DEFAULT_NARRATION = defaultMessage -> defaultMessage.get().copy();

    protected final OnPress onPress;
    protected final CreateNarration createNarration;

    public static Builder builder(Component component, OnPress onPress) {
        return new Builder(component, onPress);
    }

    public VanillaLikeButton(int x, int y, int width, int height, Component message,
                             ColorTheme colorTheme,
                             OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, colorTheme);
        this.onPress = onPress;
        this.createNarration = createNarration;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> VanillaLikeButton.super.createNarrationMessage());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private @Nullable Tooltip tooltip;
        private CreateNarration createNarration = DEFAULT_NARRATION;
        private ColorTheme colorTheme = ColorTheme.GRAY;
        private boolean drawTextShadow = true;
        private @Nullable SoundEvent pressSound;

        private Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
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

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public Builder colorTheme(ColorTheme colorTheme) {
            this.colorTheme = colorTheme;
            return this;
        }

        public Builder color(ColorTheme colorTheme) {
            return this.colorTheme(colorTheme);
        }

        public Builder drawTextShadow(boolean drawTextShadow) {
            this.drawTextShadow = drawTextShadow;
            return this;
        }

        public Builder pressSound(@Nullable SoundEvent pressSound) {
            this.pressSound = pressSound;
            return this;
        }

        public VanillaLikeButton build() {
            VanillaLikeButton button = new VanillaLikeButton(
                    this.x, this.y, this.width, this.height,
                    this.message, this.colorTheme,
                    this.onPress, this.createNarration
            );
            button.setTooltip(this.tooltip);
            button.setDrawTextShadow(this.drawTextShadow);
            button.setPressSound(this.pressSound);
            return button;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> defaultMessage);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(VanillaLikeButton button);
    }
}

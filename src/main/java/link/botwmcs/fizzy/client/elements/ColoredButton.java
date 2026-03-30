package link.botwmcs.fizzy.client.elements;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ColoredButton extends ColoredAbstractButton {
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected static final CreateNarration DEFAULT_NARRATION = (supplier) -> supplier.get().copy();

    protected final OnPress onPress;
    protected final CreateNarration createNarration;

    public static Builder builder(Component component, OnPress onPress) {
        return new Builder(component, onPress);
    }

    public ColoredButton(int x, int y, int width, int height, Component message, Color color, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, color);
        this.onPress = onPress;
        this.createNarration = createNarration;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    // ===========================
    // Builder
    // ===========================
    public static class Builder {
        private final Component message;
        public final OnPress onPress;
        private @Nullable Tooltip tooltip;
        private int x;
        private int y;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private CreateNarration createNarration;
        private Color color = Color.BLUE;

        public Builder(Component component, OnPress onPress) {
            this.createNarration = DEFAULT_NARRATION;
            this.message = component;
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

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder createNarration(CreateNarration createNarration) {
            this.createNarration = createNarration;
            return this;
        }

        public ColoredButton build() {
            ColoredButton button = new ColoredButton(
                    this.x, this.y, this.width, this.height,
                    this.message, this.color, this.onPress, this.createNarration
            );
            button.setTooltip(this.tooltip);
            return button;
        }
    }

    public interface CreateNarration {
        /**
         * @param defaultMessage 供应父类默认旁白文本（通常是按钮文本 + 提示）
         * @return 要用于旁白系统的最终文本
         */
        MutableComponent createNarrationMessage(Supplier<MutableComponent> defaultMessage);
    }

    public interface OnPress {
        void onPress(ColoredButton button);
    }
}

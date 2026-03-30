package link.botwmcs.fizzy.client.elements;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class WidgetButton extends WidgetAbstractButton {
    public static final int DEFAULT_WIDTH = 15;
    public static final int DEFAULT_HEIGHT = 14;

    private final OnPress onPress;
    public static Builder builder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public WidgetButton(int x, int y, int width, int height, Component message,
                        WidgetType type, WidgetColor color, ArrowDirection direction,
                        boolean stretchToFit, OnPress onPress) {
        super(x, y, width, height, message, type, color, direction, stretchToFit);
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

    public interface OnPress {
        void onPress(WidgetButton button);
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;
        private WidgetType type = WidgetType.LONG_ARROW;
        private WidgetColor color = WidgetColor.GRAY;
        private ArrowDirection direction = ArrowDirection.LEFT;
        private boolean stretchToFit;
        private @Nullable Tooltip tooltip;

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

        public Builder type(WidgetType type) {
            this.type = type;
            return this;
        }

        public Builder color(WidgetColor color) {
            this.color = color;
            return this;
        }

        public Builder direction(ArrowDirection direction) {
            this.direction = direction;
            return this;
        }

        public Builder stretchToFit(boolean stretchToFit) {
            this.stretchToFit = stretchToFit;
            return this;
        }

        public WidgetButton build() {
            WidgetButton button = new WidgetButton(
                    this.x,
                    this.y,
                    this.width,
                    this.height,
                    this.message,
                    this.type,
                    this.color,
                    this.direction,
                    this.stretchToFit,
                    this.onPress
            );
            button.setTooltip(this.tooltip);
            return button;
        }
    }
}

package link.botwmcs.fizzy.ui.element.button;

import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.ColoredButton;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ColoredButtonElement implements ElementPainter {
    private final Component message;
    private final ColoredButton.OnPress onPress;
    private final @Nullable Tooltip tooltip;
    private final Consumer<ColoredButton.Builder> builderCustomizer;
    private final Consumer<ColoredButton> buttonConsumer;
    private final ColoredAbstractButton.Color color;
    private final @Nullable SoundEvent pressSound;

    private ColoredButton button;

    private ColoredButtonElement(Builder builder) {
        this.message = builder.message;
        this.onPress = builder.onPress;
        this.tooltip = builder.tooltip;
        this.builderCustomizer = builder.builderCustomizer;
        this.buttonConsumer = builder.buttonConsumer;
        this.color = builder.color != null ? builder.color : ColoredAbstractButton.Color.BLUE;
        this.pressSound = builder.pressSound;
    }

    public static Builder builder(Component message, ColoredButton.OnPress onPress) {
        return new Builder(message, onPress);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.button = null;
        ColoredButton.Builder builder = ColoredButton.builder(this.message, this.onPress);
        this.builderCustomizer.accept(builder);
        builder.bounds(leftPx, topPx, widthPx, heightPx);
        builder.color(this.color);
        if (this.tooltip != null) {
            builder.tooltip(this.tooltip);
        }
        ColoredButton built = builder.build();
        if (this.pressSound != null) {
            built.setPressSound(this.pressSound);
        }
        this.button = built;
        this.buttonConsumer.accept(built);
        context.addRenderableWidget(built);
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (this.button == null) {
            return;
        }
        this.button.setX(leftPx);
        this.button.setY(topPx);
        this.button.setWidth(widthPx);
        this.button.setHeight(heightPx);
    }

    @Override
    public ElementType type() {
        return ElementType.BUTTON;
    }

    @Override
    public List<AbstractWidget> widgets() {
        return this.button == null ? List.of() : List.of(this.button);
    }

    @Nullable
    public ColoredButton button() {
        return this.button;
    }

    public static final class Builder {
        private final Component message;
        private final ColoredButton.OnPress onPress;
        private @Nullable Tooltip tooltip;
        private Consumer<ColoredButton.Builder> builderCustomizer = builder -> {};
        private Consumer<ColoredButton> buttonConsumer = button -> {};
        private @Nullable ColoredAbstractButton.Color color;
        private @Nullable SoundEvent pressSound;

        private Builder(Component message, ColoredButton.OnPress onPress) {
            this.message = Objects.requireNonNull(message, "message");
            this.onPress = Objects.requireNonNull(onPress, "onPress");
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder tooltip(Component component) {
            return this.tooltip(Tooltip.create(component));
        }

        public Builder color(ColoredAbstractButton.Color color) {
            this.color = color;
            return this;
        }

        public Builder pressSound(SoundEvent sound) {
            this.pressSound = Objects.requireNonNull(sound, "sound");
            return this;
        }

        public Builder customize(Consumer<ColoredButton.Builder> customizer) {
            Objects.requireNonNull(customizer, "customizer");
            this.builderCustomizer = this.builderCustomizer.andThen(customizer);
            return this;
        }

        public Builder applyToButton(Consumer<ColoredButton> consumer) {
            Objects.requireNonNull(consumer, "consumer");
            this.buttonConsumer = this.buttonConsumer.andThen(consumer);
            return this;
        }

        public ColoredButtonElement build() {
            return new ColoredButtonElement(this);
        }
    }
}

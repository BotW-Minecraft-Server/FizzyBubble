package link.botwmcs.fizzy.ui.element.button;

import link.botwmcs.fizzy.Fizzy;
import link.botwmcs.fizzy.client.elements.WidgetAbstractButton;
import link.botwmcs.fizzy.client.elements.WidgetButton;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class WidgetButtonElement implements ElementPainter {
    private final Component message;
    private final WidgetButton.OnPress onPress;
    private final WidgetAbstractButton.WidgetType type;
    private final WidgetAbstractButton.WidgetColor color;
    private final WidgetAbstractButton.ArrowDirection direction;
    private final boolean stretchToFit;
    private final @Nullable Tooltip tooltip;
    private final Consumer<WidgetButton.Builder> builderCustomizer;
    private final Consumer<WidgetButton> buttonConsumer;
    private final @Nullable SoundEvent pressSound;

    private WidgetButton button;

    public WidgetButtonElement(Builder builder) {
        this.message = builder.message;
        this.onPress = builder.onPress;
        this.type = builder.type;
        this.color = builder.color;
        this.direction = builder.direction;
        this.stretchToFit = builder.stretchToFit;
        this.tooltip = builder.tooltip;
        this.builderCustomizer = builder.builderCustomizer;
        this.buttonConsumer = builder.buttonConsumer;
        this.pressSound = builder.pressSound;
    }

    public static Builder builder(Component message, WidgetButton.OnPress onPress) {
        return new Builder(message, onPress);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.button = null;
        WidgetButton.Builder builder = WidgetButton.builder(this.message, this.onPress);
        this.builderCustomizer.accept(builder);
        builder.bounds(leftPx, topPx, widthPx, heightPx);
        builder.type(this.type);
        builder.color(this.color);
        builder.direction(this.direction);
        builder.stretchToFit(this.stretchToFit);
        if (this.tooltip != null) {
            builder.tooltip(this.tooltip);
        }
        WidgetButton built = builder.build();
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
    public WidgetButton button() {
        return this.button;
    }


    public static final class Builder {
        private final Component message;
        private final WidgetButton.OnPress onPress;
        private WidgetAbstractButton.WidgetType type = WidgetAbstractButton.WidgetType.LONG_ARROW;
        private WidgetAbstractButton.WidgetColor color = WidgetAbstractButton.WidgetColor.GRAY;
        private WidgetAbstractButton.ArrowDirection direction = WidgetAbstractButton.ArrowDirection.LEFT;
        private boolean stretchToFit;
        private @Nullable Tooltip tooltip;
        private Consumer<WidgetButton.Builder> builderCustomizer = builder -> {};
        private Consumer<WidgetButton> buttonConsumer = button -> {};
        private @Nullable SoundEvent pressSound;

        private Builder(Component message, WidgetButton.OnPress onPress) {
            this.message = Objects.requireNonNull(message, "message");
            this.onPress = Objects.requireNonNull(onPress, "onPress");
        }

        public Builder type(WidgetAbstractButton.WidgetType type) {
            this.type = Objects.requireNonNull(type, "type");
            return this;
        }

        public Builder color(WidgetAbstractButton.WidgetColor color) {
            this.color = Objects.requireNonNull(color, "color");
            return this;
        }

        public Builder direction(WidgetAbstractButton.ArrowDirection direction) {
            this.direction = Objects.requireNonNull(direction, "direction");
            return this;
        }

        public Builder stretchToFit(boolean stretch) {
            this.stretchToFit = stretch;
            return this;
        }

        public Builder stretchToFit() {
            return this.stretchToFit(true);
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder tooltip(Component component) {
            return this.tooltip(Tooltip.create(component));
        }

        public Builder pressSound(SoundEvent sound) {
            this.pressSound = Objects.requireNonNull(sound, "sound");
            return this;
        }

        public Builder customize(Consumer<WidgetButton.Builder> customizer) {
            Objects.requireNonNull(customizer, "customizer");
            this.builderCustomizer = this.builderCustomizer.andThen(customizer);
            return this;
        }

        public Builder applyToButton(Consumer<WidgetButton> consumer) {
            Objects.requireNonNull(consumer, "consumer");
            this.buttonConsumer = this.buttonConsumer.andThen(consumer);
            return this;
        }

        public WidgetButtonElement build() {
            return new WidgetButtonElement(this);
        }
    }

}

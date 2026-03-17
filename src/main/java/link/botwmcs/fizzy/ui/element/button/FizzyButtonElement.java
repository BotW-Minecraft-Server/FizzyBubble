package link.botwmcs.fizzy.ui.element.button;

import link.botwmcs.fizzy.client.elements.FizzyButton;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
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

public final class FizzyButtonElement implements ElementPainter {
    private final Component message;
    private final FizzyButton.OnPress onPress;
    private final @Nullable Tooltip tooltip;
    private final Consumer<FizzyButton.Builder> builderCustomizer;
    private final Consumer<FizzyButton> buttonConsumer;
    private final @Nullable SoundEvent pressSound;

    private FizzyButton button;

    private FizzyButtonElement(Builder builder) {
        this.message = builder.message;
        this.onPress = builder.onPress;
        this.tooltip = builder.tooltip;
        this.builderCustomizer = builder.builderCustomizer;
        this.buttonConsumer = builder.buttonConsumer;
        this.pressSound = builder.pressSound;
    }

    public static Builder builder(Component message, FizzyButton.OnPress onPress) {
        return new Builder(message, onPress);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.button = null;
        FizzyButton.Builder builder = FizzyButton.builder(this.message, this.onPress);
        this.builderCustomizer.accept(builder);
        builder.bounds(leftPx, topPx, widthPx, heightPx);
        if (this.tooltip != null) {
            builder.tooltip(this.tooltip);
        }
        FizzyButton built = builder.build();
        if (this.pressSound != null) {
            built.setPressSound(this.pressSound);
        }
        this.button = built;
        this.buttonConsumer.accept(built);
        context.addRenderableWidget(built);
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        FizzyGuiUtils.syncWidgetBounds(this.button, leftPx, topPx, widthPx, heightPx);
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
    public FizzyButton button() {
        return this.button;
    }

    public static final class Builder {
        private final Component message;
        private final FizzyButton.OnPress onPress;
        private @Nullable Tooltip tooltip;
        private Consumer<FizzyButton.Builder> builderCustomizer = builder -> {};
        private Consumer<FizzyButton> buttonConsumer = button -> {};
        private @Nullable SoundEvent pressSound;

        private Builder(Component message, FizzyButton.OnPress onPress) {
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

        public Builder pressSound(SoundEvent sound) {
            this.pressSound = Objects.requireNonNull(sound, "sound");
            return this;
        }

        public Builder customize(Consumer<FizzyButton.Builder> customizer) {
            Objects.requireNonNull(customizer, "customizer");
            this.builderCustomizer = this.builderCustomizer.andThen(customizer);
            return this;
        }

        public Builder applyToButton(Consumer<FizzyButton> consumer) {
            Objects.requireNonNull(consumer, "consumer");
            this.buttonConsumer = this.buttonConsumer.andThen(consumer);
            return this;
        }

        public FizzyButtonElement build() {
            return new FizzyButtonElement(this);
        }
    }
}

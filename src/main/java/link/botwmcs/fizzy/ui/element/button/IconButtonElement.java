package link.botwmcs.fizzy.ui.element.button;

import link.botwmcs.fizzy.client.elements.CustomIconButton;
import link.botwmcs.fizzy.client.util.FizzyGuiUtils;
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

public final class IconButtonElement implements ElementPainter {
    private final Component message;
    private final CustomIconButton.OnPress onPress;
    private final ResourceLocation texture;
    private final boolean stretchToFit;
    private final boolean allowUpscale;
    private final @Nullable Tooltip tooltip;
    private final Consumer<CustomIconButton.Builder> builderCustomizer;
    private final Consumer<CustomIconButton> buttonConsumer;
    private final @Nullable SoundEvent pressSound;

    private CustomIconButton button;

    private IconButtonElement(Builder builder) {
        this.message = builder.message;
        this.onPress = builder.onPress;
        this.texture = builder.texture;
        this.stretchToFit = builder.stretchToFit;
        this.allowUpscale = builder.allowUpscale;
        this.tooltip = builder.tooltip;
        this.builderCustomizer = builder.builderCustomizer;
        this.buttonConsumer = builder.buttonConsumer;
        this.pressSound = builder.pressSound;
    }

    public static Builder builder(Component message, CustomIconButton.OnPress onPress, ResourceLocation texture) {
        return new Builder(message, onPress, texture);
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        this.button = null;
        CustomIconButton.Builder builder = CustomIconButton.builder(this.message, this.onPress, this.texture);
        this.builderCustomizer.accept(builder);
        builder.bounds(leftPx, topPx, widthPx, heightPx);
        builder.stretchToFit(this.stretchToFit);
        builder.allowUpscale(this.allowUpscale);
        if (this.tooltip != null) {
            builder.tooltip(this.tooltip);
        }
        CustomIconButton built = builder.build();
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
    public CustomIconButton button() {
        return this.button;
    }

    public static final class Builder {
        private final Component message;
        private final CustomIconButton.OnPress onPress;
        private final ResourceLocation texture;
        private boolean stretchToFit;
        private boolean allowUpscale;
        private @Nullable Tooltip tooltip;
        private Consumer<CustomIconButton.Builder> builderCustomizer = builder -> {};
        private Consumer<CustomIconButton> buttonConsumer = button -> {};
        private @Nullable SoundEvent pressSound;

        private Builder(Component message, CustomIconButton.OnPress onPress, ResourceLocation texture) {
            this.message = Objects.requireNonNull(message, "message");
            this.onPress = Objects.requireNonNull(onPress, "onPress");
            this.texture = Objects.requireNonNull(texture, "texture");
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

        public Builder tooltip(Component component) {
            return this.tooltip(Tooltip.create(component));
        }

        public Builder pressSound(SoundEvent sound) {
            this.pressSound = Objects.requireNonNull(sound, "sound");
            return this;
        }

        public Builder customize(Consumer<CustomIconButton.Builder> customizer) {
            Objects.requireNonNull(customizer, "customizer");
            this.builderCustomizer = this.builderCustomizer.andThen(customizer);
            return this;
        }

        public Builder applyToButton(Consumer<CustomIconButton> consumer) {
            Objects.requireNonNull(consumer, "consumer");
            this.buttonConsumer = this.buttonConsumer.andThen(consumer);
            return this;
        }

        public IconButtonElement build() {
            return new IconButtonElement(this);
        }
    }
}

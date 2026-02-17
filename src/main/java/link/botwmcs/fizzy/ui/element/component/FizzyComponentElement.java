package link.botwmcs.fizzy.ui.element.component;

import link.botwmcs.fizzy.client.util.TextRenderer;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.animate.AnimatableElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class FizzyComponentElement implements AnimatableElement {
    private final TextRenderer renderer;

    private FizzyComponentElement(Builder builder) {
        this.renderer = builder.buildRenderer();
    }

    public static Builder builder(Component text) {
        return new Builder(text);
    }

    public static FizzyComponentElement singleLine(Component text) {
        return builder(text).singleLine().build();
    }

    public static FizzyComponentElement singleLine(String text) {
        return singleLine(Component.literal(text));
    }

    public static FizzyComponentElement multiLine(Component text) {
        return builder(text).multiLine().wrap(true).build();
    }

    public static FizzyComponentElement multiLine(String text) {
        return multiLine(Component.literal(text));
    }

    public static FizzyComponentElement multiLine(List<Component> lines) {
        return builder(Component.empty()).lines(lines).multiLine().wrap(false).build();
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        renderer.render(g, leftPx, topPx, widthPx, heightPx, partialTick);
    }

    @Override
    public ElementType type() {
        return ElementType.COMPONENT;
    }

    public static final class Builder extends TextRenderer.Builder<Builder> {
        private Builder(Component text) {
            super(text);
        }

        public FizzyComponentElement build() {
            return new FizzyComponentElement(this);
        }
    }
}

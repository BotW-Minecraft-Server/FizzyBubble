package link.botwmcs.fizzy.ui.element.below;

import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.ColoredButton;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class RightButtonBelow implements ElementPainter {
    private static final int BUTTON_WIDTH = 54;
    private static final int BUTTON_HEIGHT = 13;
    private static final int OFFSET_X = 98;

    private final ColoredButtonElement button;

    public RightButtonBelow(Component message, ColoredButton.OnPress onPress) {
        this(message, onPress, builder -> {});
    }

    public RightButtonBelow(Component message, ColoredButton.OnPress onPress, Consumer<ColoredButtonElement.Builder> customizer) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(onPress, "onPress");
        Objects.requireNonNull(customizer, "customizer");
        ColoredButtonElement.Builder builder = ColoredButtonElement.builder(message, onPress)
                .color(ColoredAbstractButton.Color.BLUE);
        customizer.accept(builder);
        this.button = builder.build();
    }

    public RightButtonBelow(ColoredButtonElement element) {
        this.button = Objects.requireNonNull(element, "element");
    }

    public ColoredButtonElement button() {
        return button;
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        button.init(context, leftPx + OFFSET_X, topPx, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        button.render(g, leftPx + OFFSET_X, topPx, BUTTON_WIDTH, BUTTON_HEIGHT, partialTick);
    }

    @Override
    public ElementType type() {
        return ElementType.BUTTON;
    }

    @Override
    public List<AbstractWidget> widgets() {
        return button.widgets();
    }
}

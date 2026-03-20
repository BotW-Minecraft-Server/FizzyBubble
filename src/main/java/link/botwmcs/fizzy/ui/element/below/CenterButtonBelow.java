package link.botwmcs.fizzy.ui.element.below;

import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.ColoredButton;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.ElementType;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import link.botwmcs.fizzy.ui.kernel.layout.LayoutAlign;
import link.botwmcs.fizzy.ui.kernel.layout.LayoutDsl;
import link.botwmcs.fizzy.ui.kernel.layout.LayoutModifier;
import link.botwmcs.fizzy.ui.kernel.layout.LayoutTreePainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class CenterButtonBelow implements ElementPainter {
    private static final int BUTTON_WIDTH = 54;
    private static final int BUTTON_HEIGHT = 13;

    private final ColoredButtonElement button;
    private final LayoutTreePainter layout;

    public CenterButtonBelow(Component message, ColoredButton.OnPress onPress) {
        this(message, onPress, builder -> {
        });
    }

    public CenterButtonBelow(Component message, ColoredButton.OnPress onPress, Consumer<ColoredButtonElement.Builder> customizer) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(onPress, "onPress");
        Objects.requireNonNull(customizer, "customizer");
        ColoredButtonElement.Builder builder = ColoredButtonElement.builder(message, onPress)
                .color(ColoredAbstractButton.Color.ORANGE);
        customizer.accept(builder);
        this.button = builder.build();
        this.layout = buildLayout(button);
    }

    public CenterButtonBelow(ColoredButtonElement element) {
        this.button = Objects.requireNonNull(element, "element");
        this.layout = buildLayout(button);
    }

    public ColoredButtonElement button() {
        return button;
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        layout.init(context, leftPx, topPx, widthPx, heightPx);
    }

    @Override
    public void render(GuiGraphics graphics, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        layout.render(graphics, leftPx, topPx, widthPx, heightPx, partialTick);
    }

    @Override
    public ElementType type() {
        return ElementType.BUTTON;
    }

    @Override
    public List<AbstractWidget> widgets() {
        return button.widgets();
    }

    private static LayoutTreePainter buildLayout(ColoredButtonElement button) {
        LayoutModifier buttonModifier = LayoutDsl.modifier()
                .sizePx(BUTTON_WIDTH, BUTTON_HEIGHT)
                .grow(0.0f);
        LayoutModifier spacerModifier = LayoutDsl.modifier()
                .fillWidth()
                .fillHeight()
                .grow(1.0f);

        return LayoutDsl.painter(LayoutDsl.row(LayoutDsl.modifier().fillWidth().fillHeight(), 0, LayoutAlign.START, row -> {
            row.spacer(spacerModifier);
            row.element(button, buttonModifier);
            row.spacer(spacerModifier);
        }));
    }
}

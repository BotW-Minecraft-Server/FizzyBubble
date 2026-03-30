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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class DoubleButtonBelow implements ElementPainter {
    private static final int BUTTON_WIDTH = 54;
    private static final int BUTTON_HEIGHT = 13;
    private static final int LEADING_SPACE = 26;
    private static final int BETWEEN_SPACE = 18;

    private final ColoredButtonElement leftButton;
    private final ColoredButtonElement rightButton;
    private final LayoutTreePainter layout;

    public DoubleButtonBelow(Component leftMessage, ColoredButton.OnPress leftPress,
                             Component rightMessage, ColoredButton.OnPress rightPress) {
        this(leftMessage, leftPress, builder -> {}, rightMessage, rightPress, builder -> {});
    }

    public DoubleButtonBelow(Component leftMessage, ColoredButton.OnPress leftPress,
                             Consumer<ColoredButtonElement.Builder> leftCustomizer,
                             Component rightMessage, ColoredButton.OnPress rightPress,
                             Consumer<ColoredButtonElement.Builder> rightCustomizer) {
        Objects.requireNonNull(leftMessage, "leftMessage");
        Objects.requireNonNull(leftPress, "leftPress");
        Objects.requireNonNull(rightMessage, "rightMessage");
        Objects.requireNonNull(rightPress, "rightPress");
        Objects.requireNonNull(leftCustomizer, "leftCustomizer");
        Objects.requireNonNull(rightCustomizer, "rightCustomizer");

        ColoredButtonElement.Builder leftBuilder = ColoredButtonElement.builder(leftPress)
                .text(leftMessage)
                .color(ColoredAbstractButton.Color.ORANGE);
        leftCustomizer.accept(leftBuilder);
        this.leftButton = leftBuilder.build();

        ColoredButtonElement.Builder rightBuilder = ColoredButtonElement.builder(rightPress)
                .text(rightMessage)
                .color(ColoredAbstractButton.Color.BLUE);
        rightCustomizer.accept(rightBuilder);
        this.rightButton = rightBuilder.build();
        this.layout = buildLayout(this.leftButton, this.rightButton);
    }

    public DoubleButtonBelow(ColoredButtonElement leftButton, ColoredButtonElement rightButton) {
        this.leftButton = Objects.requireNonNull(leftButton, "leftButton");
        this.rightButton = Objects.requireNonNull(rightButton, "rightButton");
        this.layout = buildLayout(this.leftButton, this.rightButton);
    }

    public ColoredButtonElement leftButton() {
        return leftButton;
    }

    public ColoredButtonElement rightButton() {
        return rightButton;
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        layout.init(context, leftPx, topPx, widthPx, heightPx);
    }

    @Override
    public void render(GuiGraphicsExtractor g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        layout.render(g, leftPx, topPx, widthPx, heightPx, partialTick);
    }

    @Override
    public ElementType type() {
        return ElementType.BUTTON;
    }

    @Override
    public List<AbstractWidget> widgets() {
        List<AbstractWidget> out = new ArrayList<>();
        out.addAll(leftButton.widgets());
        out.addAll(rightButton.widgets());
        return out;
    }

    private static LayoutTreePainter buildLayout(ColoredButtonElement leftButton, ColoredButtonElement rightButton) {
        LayoutModifier buttonModifier = LayoutDsl.modifier()
                .sizePx(BUTTON_WIDTH, BUTTON_HEIGHT)
                .grow(0.0f);
        LayoutModifier fillModifier = LayoutDsl.modifier()
                .fillWidth()
                .fillHeight()
                .grow(1.0f);

        return LayoutDsl.painter(LayoutDsl.row(LayoutDsl.modifier().fillWidth().fillHeight(), 0, LayoutAlign.START, row -> {
            row.spacer(LayoutDsl.modifier().widthPx(LEADING_SPACE).fillHeight().grow(0.0f));
            row.element(leftButton, buttonModifier);
            row.spacer(LayoutDsl.modifier().widthPx(BETWEEN_SPACE).fillHeight().grow(0.0f));
            row.element(rightButton, buttonModifier);
            row.spacer(fillModifier);
        }));
    }
}

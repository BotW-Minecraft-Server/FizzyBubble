package link.botwmcs.fizzy.ui.below;

import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.ColoredButton;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

public final class DoubleButtonBelow implements ElementPainter {
    private static final int BUTTON_WIDTH = 54;
    private static final int BUTTON_HEIGHT = 13;
    private static final int LEFT_OFFSET_X = 26;
    private static final int RIGHT_OFFSET_X = 98;

    private final ColoredButtonElement leftButton;
    private final ColoredButtonElement rightButton;

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

        ColoredButtonElement.Builder leftBuilder = ColoredButtonElement.builder(leftMessage, leftPress)
                .color(ColoredAbstractButton.Color.ORANGE);
        leftCustomizer.accept(leftBuilder);
        this.leftButton = leftBuilder.build();

        ColoredButtonElement.Builder rightBuilder = ColoredButtonElement.builder(rightMessage, rightPress)
                .color(ColoredAbstractButton.Color.BLUE);
        rightCustomizer.accept(rightBuilder);
        this.rightButton = rightBuilder.build();
    }

    public DoubleButtonBelow(ColoredButtonElement leftButton, ColoredButtonElement rightButton) {
        this.leftButton = Objects.requireNonNull(leftButton, "leftButton");
        this.rightButton = Objects.requireNonNull(rightButton, "rightButton");
    }

    public ColoredButtonElement leftButton() {
        return leftButton;
    }

    public ColoredButtonElement rightButton() {
        return rightButton;
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        leftButton.init(context, leftPx + LEFT_OFFSET_X, topPx, BUTTON_WIDTH, BUTTON_HEIGHT);
        rightButton.init(context, leftPx + RIGHT_OFFSET_X, topPx, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        leftButton.render(g, leftPx + LEFT_OFFSET_X, topPx, BUTTON_WIDTH, BUTTON_HEIGHT, partialTick);
        rightButton.render(g, leftPx + RIGHT_OFFSET_X, topPx, BUTTON_WIDTH, BUTTON_HEIGHT, partialTick);
    }
}

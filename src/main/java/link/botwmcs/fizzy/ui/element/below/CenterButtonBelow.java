package link.botwmcs.fizzy.ui.element.below;

import link.botwmcs.fizzy.client.elements.ColoredAbstractButton;
import link.botwmcs.fizzy.client.elements.ColoredButton;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.element.button.ColoredButtonElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

public final class CenterButtonBelow implements ElementPainter {
    private static final int BUTTON_WIDTH = 54;
    private static final int BUTTON_HEIGHT = 13;

    private final ColoredButtonElement button;
    public CenterButtonBelow(Component message, ColoredButton.OnPress onPress) {
        this(message, onPress, builder -> {});
    }

    public CenterButtonBelow(Component message, ColoredButton.OnPress onPress, Consumer<ColoredButtonElement.Builder> customizer) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(onPress, "onPress");
        Objects.requireNonNull(customizer, "customizer");
        ColoredButtonElement.Builder builder = ColoredButtonElement.builder(message, onPress)
                .color(ColoredAbstractButton.Color.ORANGE);
        customizer.accept(builder);
        this.button = builder.build();
    }

    public CenterButtonBelow(ColoredButtonElement element) {
        this.button = Objects.requireNonNull(element, "element");
    }

    public ColoredButtonElement button() {
        return button;
    }


    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        int btnLeft = leftPx + (widthPx - BUTTON_WIDTH) / 2;
        int btnTop = topPx + heightPx - BUTTON_HEIGHT - 4; // 底边留 4px 间距
        button.init(context, btnLeft, btnTop, BUTTON_WIDTH, BUTTON_HEIGHT);
    }
    @Override
    public void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        int offset = Math.max(0, (widthPx - BUTTON_WIDTH) / 2);
        button.render(g, leftPx + offset, topPx, BUTTON_WIDTH, BUTTON_HEIGHT, partialTick);
    }
}

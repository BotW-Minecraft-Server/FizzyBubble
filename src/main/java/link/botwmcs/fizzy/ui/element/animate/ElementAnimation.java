package link.botwmcs.fizzy.ui.element.animate;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface ElementAnimation {
    default void init(ElementPainter.InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
    }

    default void tick(ElementAnimationContext context) {
    }

    default void apply(ElementAnimationContext context, ElementTransform transform) {
    }

    default void beforeRender(ElementAnimationContext context, GuiGraphicsExtractor g) {
    }

    default void afterRender(ElementAnimationContext context, GuiGraphicsExtractor g) {
    }
}

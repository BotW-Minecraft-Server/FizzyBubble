package link.botwmcs.fizzy.ui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

@FunctionalInterface
public interface ElementPainter {
    default void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {}
    void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick);

    interface InitContext {
        <T extends AbstractWidget> T addRenderableWidget(T widget);
    }

}

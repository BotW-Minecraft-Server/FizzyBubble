package link.botwmcs.fizzy.ui.below;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

public interface BelowPainter {
    default void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {}
    void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick);

    interface InitContext {
        <T extends AbstractWidget> T addRenderableWidget(T widget);
    }
}

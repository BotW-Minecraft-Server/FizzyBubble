package link.botwmcs.fizzy.ui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

@FunctionalInterface
public interface ElementPainter {
    default void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {}
    void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick);
    default ElementType type() { return ElementType.CUSTOM; }
    default List<AbstractWidget> widgets() { return List.of(); }

    interface InitContext {
        <T extends AbstractWidget> T addRenderableWidget(T widget);
    }

}

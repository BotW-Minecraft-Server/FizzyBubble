package link.botwmcs.fizzy.ui.kernel.layout;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Objects;

final class LayoutLeafNode extends LayoutNode {
    private final ElementPainter element;
    private LayoutMountContext mountedContext;
    private boolean initialized;

    LayoutLeafNode(ElementPainter element, LayoutModifier modifier) {
        super(modifier);
        this.element = Objects.requireNonNull(element, "element");
    }

    @Override
    protected void onMount(LayoutMountContext context, LayoutRect bounds) {
        ensureInit(context, bounds);
    }

    @Override
    protected void onRender(LayoutMountContext context, GuiGraphics graphics, float partialTick, LayoutRect bounds) {
        ensureInit(context, bounds);
        if (!modifier().isVisible()) {
            return;
        }
        element.render(graphics, bounds.x(), bounds.y(), bounds.width(), bounds.height(), partialTick);
    }

    private void ensureInit(LayoutMountContext context, LayoutRect bounds) {
        if (mountedContext != context) {
            mountedContext = context;
            initialized = false;
        }
        if (initialized) {
            return;
        }
        element.init(context.initContext(), bounds.x(), bounds.y(), bounds.width(), bounds.height());
        initialized = true;
    }
}

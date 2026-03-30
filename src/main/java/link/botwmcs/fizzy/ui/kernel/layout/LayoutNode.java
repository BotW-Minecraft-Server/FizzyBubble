package link.botwmcs.fizzy.ui.kernel.layout;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

abstract class LayoutNode {
    private final LayoutModifier modifier;

    protected LayoutNode(LayoutModifier modifier) {
        this.modifier = modifier == null ? LayoutModifier.DEFAULT : modifier;
    }

    LayoutModifier modifier() {
        return modifier;
    }

    final void mount(LayoutMountContext context, LayoutRect bounds) {
        onMount(context, bounds);
    }

    final void render(LayoutMountContext context, GuiGraphicsExtractor graphics, float partialTick, LayoutRect bounds) {
        onRender(context, graphics, partialTick, bounds);
    }

    protected abstract void onMount(LayoutMountContext context, LayoutRect bounds);

    protected abstract void onRender(LayoutMountContext context, GuiGraphicsExtractor graphics, float partialTick, LayoutRect bounds);

    static final class LayoutMountContext {
        private final ElementPainter.InitContext initContext;

        LayoutMountContext(ElementPainter.InitContext initContext) {
            this.initContext = initContext;
        }

        ElementPainter.InitContext initContext() {
            return initContext;
        }
    }
}

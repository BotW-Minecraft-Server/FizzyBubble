package link.botwmcs.fizzy.ui.kernel.layout;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Objects;

public final class LayoutTreePainter implements ElementPainter {
    private final LayoutTree tree;
    private LayoutNode.LayoutMountContext mountContext;
    private LayoutRect lastBounds = LayoutRect.of(0, 0, 0, 0);

    public LayoutTreePainter(LayoutTree tree) {
        this.tree = Objects.requireNonNull(tree, "tree");
    }

    @Override
    public void init(InitContext context, int leftPx, int topPx, int widthPx, int heightPx) {
        mountContext = new LayoutNode.LayoutMountContext(context);
        lastBounds = LayoutRect.of(leftPx, topPx, widthPx, heightPx);
        tree.root().mount(mountContext, lastBounds);
    }

    @Override
    public void render(GuiGraphics graphics, int leftPx, int topPx, int widthPx, int heightPx, float partialTick) {
        if (mountContext == null) {
            return;
        }
        lastBounds = LayoutRect.of(leftPx, topPx, widthPx, heightPx);
        tree.root().render(mountContext, graphics, partialTick, lastBounds);
    }
}

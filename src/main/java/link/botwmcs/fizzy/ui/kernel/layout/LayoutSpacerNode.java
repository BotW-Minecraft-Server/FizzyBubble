package link.botwmcs.fizzy.ui.kernel.layout;

import net.minecraft.client.gui.GuiGraphics;

final class LayoutSpacerNode extends LayoutNode {
    LayoutSpacerNode(LayoutModifier modifier) {
        super(modifier);
    }

    @Override
    protected void onMount(LayoutMountContext context, LayoutRect bounds) {
    }

    @Override
    protected void onRender(LayoutMountContext context, GuiGraphics graphics, float partialTick, LayoutRect bounds) {
    }
}

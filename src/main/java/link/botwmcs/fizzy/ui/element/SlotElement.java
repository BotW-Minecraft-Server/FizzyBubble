package link.botwmcs.fizzy.ui.element;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface SlotElement {
    void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick);
}

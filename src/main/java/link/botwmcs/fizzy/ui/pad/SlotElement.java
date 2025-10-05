package link.botwmcs.fizzy.ui.pad;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface SlotElement {
    void render(GuiGraphics g, int leftPx, int topPx, int widthPx, int heightPx, float partialTick);
}

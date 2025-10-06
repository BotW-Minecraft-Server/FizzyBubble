package link.botwmcs.fizzy.ui.split;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Objects;

public record PixelSplitSpec(int offsetX, int offsetY, int lengthPx, SplitType type) implements SplitSpec {
    public PixelSplitSpec {
        if (lengthPx <= 0) {
            throw new IllegalArgumentException("lengthPx must be > 0");
        }
        Objects.requireNonNull(type, "type");
    }

    @Override
    public void paint(GuiGraphics g, SplitPainter painter, FramePainter.SlotArea slotArea) {
        painter.paintInSlotArea(g, slotArea, offsetX, offsetY, lengthPx, type);
    }
}

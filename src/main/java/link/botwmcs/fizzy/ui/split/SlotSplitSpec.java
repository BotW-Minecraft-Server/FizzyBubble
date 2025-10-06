package link.botwmcs.fizzy.ui.split;

import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphics;

public record SlotSplitSpec(int rowStart, int colStart, int rowEnd, int colEnd) implements SplitSpec {
    public SlotSplitSpec {
        if (rowStart <= 0 || colStart <= 0 || rowEnd <= 0 || colEnd <= 0) {
            throw new IllegalArgumentException("Slot indices must be >= 1");
        }
        if (rowStart == rowEnd && colStart == colEnd) {
            throw new IllegalArgumentException("Split start and end may not be the same slot");
        }
        if (rowStart != rowEnd && colStart != colEnd) {
            throw new IllegalArgumentException("Split must align with either a single row or a single column");
        }
    }

    @Override
    public void paint(GuiGraphics g, SplitPainter painter, FramePainter.SlotArea slotArea) {
        painter.paintBetweenSlots(g, slotArea, rowStart, colStart, rowEnd, colEnd);
    }
}

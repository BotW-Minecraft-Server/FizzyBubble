package link.botwmcs.fizzy.ui.split;

import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.frame.FramePainter;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface SplitPainter {
    void paint(GuiGraphicsExtractor g, int x, int y, int lengthPx, SplitType type);
    SplitMetrics metrics();

    default void paintInSlotArea(GuiGraphicsExtractor g, FramePainter.SlotArea area, int offsetX, int offsetY, int lengthPx, SplitType type) {
        if (area == null) {
            throw new IllegalStateException("SlotArea is null. Did you forget to call setLayout() on FramePainter?");
        }
        paint(g, area.x() + offsetX, area.y() + offsetY, lengthPx, type);
    }

    default void paintBetweenSlots(GuiGraphicsExtractor g, FramePainter.SlotArea area, int rowStart, int colStart, int rowEnd, int colEnd) {
        if (area == null) {
            throw new IllegalStateException("SlotArea is null. Did you forget to call setLayout() on FramePainter?");
        }
        if (rowStart <= 0 || colStart <= 0 || rowEnd <= 0 || colEnd <= 0) {
            throw new IllegalArgumentException("Row and column indices must be positive.");
        }
        if (rowStart == rowEnd && colStart == colEnd) {
            throw new IllegalArgumentException("Start and end positions cannot be the same.");
        }

        if (colStart == colEnd) {
            int minRow = Math.min(rowStart, rowEnd);
            int maxRow = Math.max(rowStart, rowEnd);
            int top = slotTop(area, minRow);
            int bottom = slotBottom(area, maxRow);
            int anchor = slotRightEdge(area, colStart) - 1;
            int x = anchor - metrics().anchorOffset(SplitType.VERTICAL);
            paint(g, x, top, bottom - top, SplitType.VERTICAL);
            return;
        }

        if (rowStart == rowEnd) {
            int minCol = Math.min(colStart, colEnd);
            int maxCol = Math.max(colStart, colEnd);
            int left = slotLeft(area, minCol);
            int right = slotRightEdge(area, maxCol);
            int anchor = slotBottom(area, rowStart) - 1;
            int y = anchor - metrics().anchorOffset(SplitType.HORIZONTAL);
            paint(g, left, y, right - left, SplitType.HORIZONTAL);
            return;
        }

        throw new IllegalArgumentException("Split must align with either a single row or a single column");
    }

    // Tools
    private static int slotTop(FramePainter.SlotArea area, int rowIndex) {
        return area.y() + (rowIndex - 1) * UiUnit.SLOT_PX;
    }

    private static int slotBottom(FramePainter.SlotArea area, int rowIndex) {
        return area.y() + rowIndex * UiUnit.SLOT_PX;
    }

    private static int slotLeft(FramePainter.SlotArea area, int colIndex) {
        return area.x() + (colIndex - 1) * UiUnit.SLOT_PX;
    }

    private static int slotRightEdge(FramePainter.SlotArea area, int colIndex) {
        return area.x() + colIndex * UiUnit.SLOT_PX;
    }
}

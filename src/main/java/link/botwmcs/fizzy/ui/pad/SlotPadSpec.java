package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;

import java.util.List;

public record SlotPadSpec(
        int rowStart,
        int colStart,
        int rowEnd,
        int colEnd,
        boolean inner,
        List<ElementPainter> elements
) implements PadSpec {
    public SlotPadSpec {
        if (rowStart < 1) {
            throw new IllegalArgumentException("rowStart must be >= 1");
        }
        if (colStart < 1) {
            throw new IllegalArgumentException("colStart must be >= 1");
        }
        if (rowEnd < rowStart) {
            throw new IllegalArgumentException("rowEnd must be >= rowStart");
        }
        if (colEnd < colStart) {
            throw new IllegalArgumentException("colEnd must be >= colStart");
        }
        elements = List.copyOf(elements);
    }

    public int widthSlots() {
        return colEnd - colStart + 1;
    }

    public int heightSlots() {
        return rowEnd - rowStart + 1;
    }

    public int widthPx() {
        return widthSlots() * UiUnit.SLOT_PX;
    }

    public int heightPx() {
        return heightSlots() * UiUnit.SLOT_PX;
    }

    public int slotCount() {
        return widthSlots() * heightSlots();
    }

    @Override
    public PadBounds resolve(FramePainter frame, FramePainter.SlotArea slotArea) {
        if (slotArea == null) {
            throw new IllegalStateException("Slot area is not available for slot pad.");
        }
        int padLeft = slotArea.x() + (colStart - 1) * UiUnit.SLOT_PX;
        int padTop = slotArea.y() + (rowStart - 1) * UiUnit.SLOT_PX;
        int padWidth = widthSlots() * UiUnit.SLOT_PX;
        int padHeight = heightSlots() * UiUnit.SLOT_PX;
        if (inner) {
            int inset = UiUnit.SLOT_PAD_INNER_INSET_PX;
            int insetX = Math.min(inset, padWidth / 2);
            int insetY = Math.min(inset, padHeight / 2);
            padLeft += insetX;
            padTop += insetY;
            padWidth -= insetX * 2;
            padHeight -= insetY * 2;
        }
        return new PadBounds(padLeft, padTop, padWidth, padHeight);
    }
}

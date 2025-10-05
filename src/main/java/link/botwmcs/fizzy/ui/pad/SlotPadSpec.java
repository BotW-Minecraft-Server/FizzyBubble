package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.core.UiUnit;
import link.botwmcs.fizzy.ui.element.SlotElement;

import java.util.List;

public record SlotPadSpec(
        int rowStart,
        int colStart,
        int rowEnd,
        int colEnd,
        List<SlotElement> elements
) {
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
}

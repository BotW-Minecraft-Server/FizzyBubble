package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;

import java.util.List;

public record FramePadSpec(List<ElementPainter> elements) implements PadSpec {
    public FramePadSpec {
        elements = List.copyOf(elements);
    }

    @Override
    public PadBounds resolve(FramePainter frame, FramePainter.SlotArea slotArea) {
        if (slotArea == null) {
            throw new IllegalStateException("Slot area is not available for frame pad.");
        }

        var layout = frame.layout();

        return new PadBounds(layout.left(), slotArea.y(), layout.w(), slotArea.h());
    }
}

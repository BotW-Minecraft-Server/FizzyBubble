package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;

import java.util.List;

public record PixelPadSpec(
        int offsetLeft,
        int offsetTop,
        int width,
        int height,
        List<ElementPainter> elements
) implements PadSpec {
    public PixelPadSpec {
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        }
        if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        }
        elements = List.copyOf(elements);
    }

    @Override
    public PadBounds resolve(FramePainter frame, FramePainter.SlotArea slotArea) {
        FramePainter.Layout layout = frame.layout();
        if (layout == null) {
            throw new IllegalStateException("Layout has not been set. Did you forget to call setLayout()?");
        }
        return new PadBounds(layout.left() + offsetLeft, layout.top() + offsetTop, width, height);
    }
}

package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.element.ElementPainter;
import link.botwmcs.fizzy.ui.frame.FramePainter;

import java.util.List;

public interface PadSpec {
    List<ElementPainter> elements();
    PadBounds resolve(FramePainter frame, FramePainter.SlotArea slotArea);
    record PadBounds(int left, int top, int width, int height) {}
}

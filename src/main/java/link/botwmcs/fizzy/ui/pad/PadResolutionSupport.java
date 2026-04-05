package link.botwmcs.fizzy.ui.pad;

import link.botwmcs.fizzy.ui.frame.EasyFrame;
import link.botwmcs.fizzy.ui.frame.FramePainter;

import javax.annotation.Nullable;

public final class PadResolutionSupport {
    private PadResolutionSupport() {
    }

    public static PadSpec.PadBounds resolvePadBounds(
            PadSpec pad,
            FramePainter frame,
            @Nullable FramePainter.SlotArea slotArea
    ) {
        if (frame instanceof EasyFrame && !(pad instanceof PixelPadSpec)) {
            throw new IllegalStateException("EasyFrame only supports padByPx (PixelPadSpec).");
        }
        return pad.resolve(frame, slotArea);
    }
}

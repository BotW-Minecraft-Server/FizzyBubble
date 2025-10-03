package link.botwmcs.fizzy.ui.core;

import link.botwmcs.fizzy.ui.frame.FramePainter;

public class FizzyGui {
    private final FizzyGuiSpec spec;
    private final FramePainter frame;
    private final Integer overrideW;  // 可空
    private final Integer overrideH;  // 可空

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame) {
        this(spec, frame, null, null);
    }

    public FizzyGui(FizzyGuiSpec spec, FramePainter frame, Integer overrideW, Integer overrideH) {
        this.spec = spec; this.frame = frame; this.overrideW = overrideW; this.overrideH = overrideH;
    }

    public FizzyGuiSpec spec() { return spec; }
    public FramePainter background() { return frame; }

    public int widthPx()  { return overrideW != null ? overrideW : spec.pixelWidth(); }
    public int heightPx() { return overrideH != null ? overrideH : spec.pixelHeight(); }



}
